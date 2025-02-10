#!/usr/bin/env python
# -*- coding: utf-8 -*-
from __future__ import absolute_import, division, print_function

import os
import sys
import json
import math
import argparse

try:
    import configparser
except ImportError:
    import ConfigParser as configparser

try:
    from urllib.parse import urlencode
    from urllib.request import Request, urlopen
except ImportError:
    from urllib import urlencode
    from urllib2 import Request, urlopen

import tqdm
import oauthlib.oauth1
import concurrent.futures


def get_token():
    credentials = os.path.join(os.path.expanduser('~'), '.here', 'here.credentials.properties')
    config = configparser.ConfigParser()
    with open(credentials, 'r') as f:
        config_string = '[dummy_section]\n' + f.read()
    try:
        config.read_string(config_string)
    except AttributeError:
        import StringIO
        buf = StringIO.StringIO(config_string)
        config.readfp(buf)
    key_id = config.get('dummy_section', 'here.access.key.id')
    key_secret = config.get('dummy_section', 'here.access.key.secret')
    endpoint_url = config.get('dummy_section', 'here.token.endpoint.url')
    client = oauthlib.oauth1.Client(key_id, client_secret=key_secret)
    url, headers, body = client.sign(endpoint_url, http_method='POST', headers={
        'Content-Type':  'application/x-www-form-urlencoded',
    }, body={
        'grant_type': 'client_credentials',
        'expires_in': '86400',
    })
    data = urlencode(body)
    req = Request(url, data=data.encode(), headers=headers)
    res = urlopen(req)
    body = json.loads(res.read())
    return body['access_token']


def get_apis(token, catalog_hrn):
    base_url = 'https://api-lookup.data.api.platform.here.com/lookup/v1'
    headers = dict(Authorization='Bearer {token}'.format(token=token))
    url = '{base_url}/resources/{catalog_hrn}/apis'.format(base_url=base_url, catalog_hrn=catalog_hrn)
    req = Request(url, headers=headers)
    res = urlopen(req)
    body = json.loads(res.read())
    return {x['api']: x['baseURL'] for x in body}


def get_latest_version(token, metadata_api, min_version=0):
    headers = dict(Authorization='Bearer {token}'.format(token=token))
    url = '{metadata_api}/versions/latest?startVersion={min_version}'.format(
        metadata_api=metadata_api, min_version=min_version)
    req = Request(url, headers=headers)
    res = urlopen(req)
    body = json.loads(res.read())
    return body['version']


def get_metadata(token, query_api, layer_id, partition, version):
    headers = dict(Authorization='Bearer {token}'.format(token=token))
    params = urlencode(dict(version=version, partition=partition), True)
    url = '{query_api}/layers/{layer_id}/partitions?{params}'.format(
        query_api=query_api, layer_id=layer_id, params=params)
    req = Request(url, headers=headers)
    res = urlopen(req)
    body = json.loads(res.read())
    return body['partitions']


def get_blob_data(token, blob_api, layer_id, data_handle):
    headers = dict(Authorization='Bearer {token}'.format(token=token))
    url = '{blob_api}/layers/{layer_id}/data/{data_handle}'.format(
        blob_api=blob_api, layer_id=layer_id, data_handle=data_handle)
    req = Request(url, headers=headers)
    res = urlopen(req)
    return res.read()


def get_tile_id_by_coordinates(level, lat, lng):
    d = 360.0 / 2 ** level
    y = int((lat + 90) / d)
    x = int((lng + 180) / d)
    y_bs = bin(y)[2:].zfill(level)
    x_bs = bin(x)[2:].zfill(level)
    id_bs = ''.join([v for p in zip(y_bs, x_bs) for v in p])
    tile_id = int('01' + id_bs, 2)
    return tile_id


def get_tile_id_by_bounding_box(level, bounding_box):
    min_lat, min_lng, max_lat, max_lng = bounding_box
    d = 360.0 / 2 ** level / 2
    y_count = int(math.ceil((max_lat - min_lat) / d))
    x_count = int(math.ceil((max_lng - min_lng) / d))
    y_array = [min_lat + d * y for y in range(0, y_count)]
    x_array = [min_lng + d * x for x in range(0, x_count)]
    mesh = [[(y, x) for x in x_array] for y in y_array]
    tile_id = [get_tile_id_by_coordinates(level, y, x) for L in mesh for (y, x) in L]
    return list(set(tile_id))


def read_tile_id_from_file(filename):
    with open(filename, 'r') as f:
        tile_id = list(map(int, f.read().splitlines()))
    return tile_id


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--catalog', metavar='CATALOG_HRN', type=str, required=True, help='catalog HRN')
    parser.add_argument('--layer', metavar='LAYER_ID', type=str, required=True, help='layer ID')
    parser.add_argument('--partition', metavar='PARTITION_ID[,PARTITION_ID,...]', type=str, help='partition ID(s)')
    parser.add_argument('--partition-file', metavar='FILE', type=str, help='file containing partition IDs')
    parser.add_argument('--bounding-box', metavar='SOUTH_LATITUDE,WEST_LONGITUDE,NORTH_LATITUDE,EAST_LONGITUDE',
                        type=str, help='bounding box')
    parser.add_argument('--level', metavar='LEVEL', type=int, help='level of tile')
    parser.add_argument('--version', metavar='VERSION', type=int, default=-1,
                        help='version of catalog')
    parser.add_argument('-w', '--workers', metavar='N', type=int, default=8, help='max number of workers')
    parser.add_argument('-o', '--output', metavar='DIR', type=str, default=os.getcwd(), help='output directory')
    parser.add_argument('-v', '--verbose', action='count', default=0, help='verbose mode')
    args = parser.parse_args()

    verbose = args.verbose
    catalog_hrn = args.catalog
    layer_id = args.layer
    version = args.version
    max_workers = args.workers
    output = os.path.abspath(args.output)
    assert os.path.isdir(output), '"{output}" does not exist'.format(output=output)

    partition_file = os.path.abspath(args.partition_file) if args.partition_file else None
    if partition_file:
        assert os.path.isfile(partition_file), '"{partition_file}" does not exist'.format(partition_file=partition_file)

    if verbose:
        print('Workers: {workers}'.format(workers=max_workers))
        print('Output directory: {output}'.format(output=output))
        print('Catalog HRN: {catalog_hrn}'.format(catalog_hrn=catalog_hrn))
        print('Layer ID: {layer_id}'.format(layer_id=layer_id))

    assert args.partition or args.partition_file or (args.bounding_box and args.level is not None)
    if args.bounding_box:
        level = args.level
        bounding_box = [float(x) for x in args.bounding_box.split(',')]
        assert len(bounding_box) == 4, 'invalid bounding box'
        partition = get_tile_id_by_bounding_box(level, bounding_box)
    elif args.partition_file:
        partition = read_tile_id_from_file(partition_file)
    else:
        partition = args.partition.split(',') if args.partition else []
    partition_chunks = [partition[i:i + 100] for i in range(0, len(partition), 100)]

    if verbose:
        print('Partition ID: {partition}'.format(partition=partition))

    token = get_token()
    if verbose:
        print('Token: {token}'.format(token=token))

    apis = get_apis(token, catalog_hrn)
    if version == -1:
        version = get_latest_version(token, apis['metadata'])
    if verbose:
        print('Version: {version}'.format(version=version))

    print('Get metadata:')
    metadata = []

    def get_func(token, api, layer_id, version, callback):
        def func(partition):
            metadata = get_metadata(token, api, layer_id, partition, version)
            callback()
            return metadata
        return func

    t = tqdm.tqdm(total=len(partition_chunks), smoothing=0, file=sys.stdout)
    func = get_func(token, apis['query'], layer_id, version, t.update)
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        result = list(executor.map(func, partition_chunks))
        metadata = [x for L in result for x in L]
    t.close()

    print('Download {} tile(s):'.format(len(metadata)))

    def get_func(token, api, layer_id, callback):
        def func(metadata):
            partition_id = metadata.get('partition')
            data_handle = metadata.get('dataHandle')
            blob = get_blob_data(token, api, layer_id, data_handle)
            with open(os.path.join(output, partition_id), 'wb') as f:
                f.write(blob)
            callback()
            return partition_id
        return func

    t = tqdm.tqdm(total=len(metadata), smoothing=0, file=sys.stdout)
    func = get_func(token, apis['blob'], layer_id, t.update)
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        executor.map(func, metadata)
    t.close()
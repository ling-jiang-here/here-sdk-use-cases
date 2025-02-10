## Prerequisites
Prepare your credentials and save it a your user root folder at `~/.here/` as `here.credentials.properties`

## Sample request
`> python .\tile_downloader.py -v --catalog hrn:here:data::olp-here:rib-2 --layer topology-geometry --partition 23618402 --output download`

## Sample response
```
> python .\tile_downloader.py -v --catalog hrn:here:data::olp-here:rib-2 --layer topology-geometry --partition 23618402 --output download
Workers: 8
Output directory: C:\Users\jiang1\Workspace\here-sdk-use-cases\python-map-data-downloader\download
Catalog HRN: hrn:here:data::olp-here:rib-2
Layer ID: topology-geometry
Partition ID: ['23618402']
Token: eyJh....sZfA
Version: 6960
Get metadata:
100%|██████████████████████████████████████████████████████████████████████████████████████████████████████| 1/1 [00:01<00:00,  1.09s/it]
Download 1 tile(s):
100%|██████████████████████████████████████████████████████████████████████████████████████████████████████| 1/1 [00:03<00:00,  3.40s/it]
```
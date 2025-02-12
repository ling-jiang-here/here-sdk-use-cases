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

## Data Inspector
If you doubt about your access to map data, try [HERE Data Inspector](https://platform.here.com/data-inspector/examples)
![data-inspector](https://github.com/user-attachments/assets/af9f8d0b-1b38-40ce-861b-fd0840897eec)
As you can see, the catalog, layer, and partition IDs used in the sample request are all taken from the above inspector.

# orderdb

order your record &amp; sub and push message

## the benchmark

TODO

## docker build

```bash
docker pull qyvlik/orderdb

docker run -d \
--name myorderdb \
-v /home/www/orderdb:/home/www/orderdb \
-p 17711:17711 \
qyvlik/orderdb --orderdb.disk.directory=/home/www/orderdb
```

## append

```json
{
  "id": 10001,
  "method": "append",
  "params":["test", "action-1000", {}]
}
```

- `scope`: scope of your data
- `key`: unique key of data
- `data`: the data, json format

```json
{
  "id": 10001,
  "method": "append",
  "result": 1
}
```

- `result`: the index of the `key`


## delete.by.index

```json
{
  "id": 10001,
  "method": "delete.by.index",
  "params":["test", 1000]
}
```

- `scope`: scope of your data
- `index`: index of data

```json
{
  "id": 10001,
  "method": "append",
  "result": "success"
}
```

- `result`: success or failure

## delete.by.key

```json
{
  "id": 10001,
  "method": "delete.by.key",
  "params":["test", "action-1000"]
}
```

- `scope`: scope of your data
- `key`: key of data

```json
{
  "id": 10001,
  "method": "append",
  "result": "success"
}
```

- `result`: success or failure

## append.list

```json
{
  "id": 10001,
  "method": "append.list",
  "params":["test", true, [{"scope":"test", "key":"key1"},{"scope":"test", "key":"key2"}]]
}
```

- `scope`: scope of your data
- `ignoreExist`: ignore if the key is exist, or throw a exception
- `list`: data of list

```json
{
  "id": 10001,
  "method": "append",
  "result": [
    {
      "scope": "test",
      "key": "key1",
      "index": 1
    },
    {
      "scope": "test",
      "key": "key2",
      "index": 2
    }
  ]
}
```

- `result`: the index of the `key`

## get.by.index

```json
{
  "id": 10001,
  "method": "get.by.index",
  "params":["test", 1]
}
```

- `scope`: scope of your data
- `index`: index of data

response:

```json
{
  "id": 10001,
  "method": "get.by.index",
  "result":{
    "scope": "test",
    "key": "action-1000",
    "index": 1,
    "data":{}
  }
}
```

- `key`: the unique key
- `index`: index number
- `data`: data

## get.by.key

```json
{
  "id": 10001,
  "method": "get.by.key",
  "params":["test", "action-1000"]
}
```

- `scope`: scope of your data
- `key`: unique key of data

response:

```json
{
  "id": 10001,
  "method": "get.by.key",
  "result":{
    "scope": "test",
    "key": "action-1000",
    "index": 1,
    "data":{}
  }
}
```

- `key`: the unique key
- `index`: index number
- `data`: data

## get.latest.index

```json
{
  "id": 10001,
  "method": "get.latest.index",
  "params": ["test"]
}
```

- `scope`: scope of your data

response

```json
{
  "id": 10001,
  "method": "get.latest.index",
  "result": 1
}
```

return the latest index number.

## get.list

```json
{
  "id": 10001,
  "method": "get.list",
  "params": ["test", 1, 2]
}
```

- `scope`: scope of your data
- `from`: index which is start
- `to`: index which is end

response

```json
{
  "id": 10001,
  "method": "get.by.key",
  "result":[
    {
      "scope": "test",
      "key": "action-1000",
      "index": 1,
      "data":{}
    },
    {
      "scope": "test",
      "key": "action-1001",
      "index": 2,
      "data":{}
    }
  ]
}
```

## sub.append

```json
{
  "channel": "sub.append",
  "subscribe": true,
  "params": ["test"]
}
```

- `scope`: which scope you want subscribe.

if you subscribe success, response as follow

```json
{
  "channel": "sub.append",
  "result": "subscribe"
}
```

when some one call `append` for order some data, will receive the data as follow

```json
{
  "channel": "sub.append",
  "result": {
    "scope": "test",
    "key": "action-1001",
    "index": 2,
    "data":{}
  }
}
```

## sub.delete

```json
{
  "channel": "sub.delete",
  "subscribe": true,
  "params": ["test"]
}
```

- `scope`: which scope you want subscribe.

if you subscribe success, response as follow

```json
{
  "channel": "sub.delete",
  "result": "subscribe"
}
```

when some one call `delete some data, will receive the data as follow

```json
{
  "channel": "sub.delete",
  "result": {
    "scope": "test",
    "key": "action-1001",
    "index": 2,
    "data":{}
  }
}
```
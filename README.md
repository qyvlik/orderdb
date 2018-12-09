# orderdb

order your record &amp; sub and push message

## sequence

```json
{
  "id": 10001,
  "method": "sequence",
  "params":["test", "action-1000", {}]
}
```

- `group`: group of your data
- `key`: unique key of data
- `data`: the data, json format

```json
{
  "id": 10001,
  "method": "sequence",
  "result": 1
}
```

- `result`: the sequence number of the `key`

## get.by.sequence

```json
{
  "id": 10001,
  "method": "get.by.sequence",
  "params":["test", 1]
}
```

- `group`: group of your data
- `seuqnce`: sequence number of data

response:

```json
{
  "id": 10001,
  "method": "get.by.sequence",
  "result":{
    "seq": 1,
    "key": "action-1000",
    "data":{}
  }
}
```

- `seq`: sequence number
- `key`: the unique key
- `data`: data

## get.by.key

```json
{
  "id": 10001,
  "method": "get.by.key",
  "params":["test", "action-1000"]
}
```

- `group`: group of your data
- `key`: unique key of data

response:

```json
{
  "id": 10001,
  "method": "get.by.key",
  "result":{
    "seq": 1,
    "key": "action-1000",
    "data":{}
  }
}
```

- `seq`: sequence number
- `key`: the unique key
- `data`: data

## get.latest.sequence

```json
{
  "id": 10001,
  "method": "get.latest.sequence",
  "params": ["test"]
}
```

- `group`: group of your data

response

```json
{
  "id": 10001,
  "method": "get.latest.sequence",
  "result": 1
}
```

return the latest sequence number.

## get.list

```json
{
  "id": 10001,
  "method": "get.list",
  "params": ["test", 1, 2]
}
```

- `group`: group of your data
- `from`: sequence number which is start
- `to`: sequence number which is end

response

```json
{
  "id": 10001,
  "method": "get.by.key",
  "result":[
    {
      "seq": 1,
      "key": "action-1000",
      "data":{}
    },
    {
      "seq": 2,
      "key": "action-1001",
      "data":{}
    }
  ]
}
```

## sub.sequence

```json
{
  "channel": "sub.sequence",
  "subscribe": true,
  "params": ["test"]
}
```

- `group`: which group you want subscribe.

if you subscribe success, response as follow

```json
{
  "channel": "sub.sequence",
  "result": "subscribe"
}
```

when some one call `sequence` for sequence some data, will receive the data as follow

```json
{
  "channel": "sub.sequence",
  "result": {
    "seq": 2,
    "key": "action-1001",
    "data":{}
  }
}
```
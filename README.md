# orderdb

order your record &amp; sub and push message

## orderdb.sequence

```json
{
  "id": 10001,
  "method": "orderdb.sequence",
  "params":["test", "action-1000", {}]
}
```

- `group`: group of your data
- `uniqueKey`: unique key of data
- `data`: the data, json format

```json
{
  "id": 10001,
  "method": "orderdb.sequence",
  "result": 1
}
```

- `result`: the sequence number of the `uniqueKey`

## orderdb.get.value.by.sequenceId

```json
{
  "id": 10001,
  "method": "orderdb.get.value.by.sequenceId",
  "params":["test", 1]
}
```

response:

```json
{
  "id": 10001,
  "method": "orderdb.get.value.by.sequenceId",
  "result":{
    "sequenceId": 1,
    "uniqueKey": "action-1000",
    "data":{}
  }
}
```

- `sequenceId`: sequence number
- `uniqueKey`: the unique key
- `data`: data

## orderdb.get.value.by.uniqueKey

```json
{
  "id": 10001,
  "method": "orderdb.get.value.by.uniqueKey",
  "params":["test", "action-1000"]
}
```

response:

```json
{
  "id": 10001,
  "method": "orderdb.get.value.by.uniqueKey",
  "result":{
    "sequenceId": 1,
    "uniqueKey": "action-1000",
    "data":{}
  }
}
```

- `sequenceId`: sequence number
- `uniqueKey`: the unique key
- `data`: data

## sub
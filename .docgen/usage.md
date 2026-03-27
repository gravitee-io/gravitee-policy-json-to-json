### For this input:

```json
{
    "_id": "57762dc6ab7d620000000001",
    "name": "name",
    "__v": 0
}
```

### And this JOLT specification:

```json
[
    {
        "operation": "shift",
        "spec": 
        {
            "_id": "id",
            "*": 
            {
              "$": "&1"
            } 
        }
    },
    {
        "operation": "remove",
        "spec": 
        {
          "__v": ""
        }
    }
]
```

The output is as follows:

```json
{
    "id": "57762dc6ab7d620000000001",
    "name": "name"
}
```
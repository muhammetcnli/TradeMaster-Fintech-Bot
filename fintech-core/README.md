# Fintech Core

Minimal backend endpoints for price and user operations.

## Endpoints

### Prices
- `GET /api/v1/prices/{symbol}`

### Users
- `POST /api/v1/users/register`
- `GET /api/v1/users/{id}`
- `GET /api/v1/users/by-chat/{chatId}`

## Sample Register Body

```json
{
  "chatId": 123456,
  "username": "demo",
  "firstName": "Demo",
  "lastName": "User"
}
```

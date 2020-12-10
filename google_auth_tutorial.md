# Google OAuth2 tutorial

1. Get `access_token` from google authorization form
2. By this `access_token` we can get google user profile info using `GET` request on `https://www.googleapis.com/oauth2/v3/userinfo` url with header:
```json
Authorization: Bearer `access_token`
```
3. And, we can convert google `access_token` to our app token by this `POST` request on `127.0.0.1:8000/auth/convert-token` with body:
```json
{
    "grant_type": "convert_token",
    "client_id": "...",
    "client_secret": "...",
    "backend": "google-oauth2",
    "token": "google-access-token"
}
```
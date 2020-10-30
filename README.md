# Lab 3 of CS263: Cyber Security

My code for lab 3, decrypting traffic caught using a man in the middle attack.

# Ex8

The original man in the middle program prints the intercepted traffic and this is an example of such traffic:

```
[server->client]: 3821
[server->client]: 6607
[client->server]: 948
[server->client]: 1830
[server->client]: UfSflGBKAt98R8Y5iVfySkS6ZUiUiD+el0z3hCpqtmE=
[client->server]: Z2izoxAmcoIoZCJUZcTsOw==
[client->server]: 0TBr1tqGCrMJ+wO8YImPa9PWwj8BWHzd2rqgd6nEtj0=
[client->server]: e7JpdlW/ItiTp81NqqrDyw==
[server->client]: 3j+Vb41raWVRVr5xY4QntJcyMTi7AkVa4rVxR87IVLI=
[client->server]: YjFlMFkwD4QN5n3+SGYLDg==
[server->client]: A5D0/C+QQUziEokRTy5dzPXaqTQrLzScd5Sa0O0nzXE=
[client->server]: e7JpdlW/ItiTp81NqqrDyw==
[server->client]: PK5Ygs1liqEev9JTCyvjr1YgNHNxzQzaFji7Air9Jmc=
[client->server]: WgW5bbRGWA+CjehQa2vzAA==
[server->client]: ek1aYtLr5FgJQ7me0dWxNkZ4rKskwbXEzo0vdj15/VDf3Iuy0KPxE4PY62QttN4vQgfmbgmarKBzWkPk+1qeNW226lw98hHmzJ2kpz0NF0c=
[client->server]: GcHALaQVJp2tWuibJaVjMQ==
[server->client]: S9YGgu/KznZ2jGyY0+sYvQ==
```

This looks like Diffie-Hellman-Merkle key exchange as:

- The first and second messages are the server sending the client the global variables `g` and `n` (not sure which order)
- The third message is the client sending the server `g^c mod n` where `c` is the client's secret number
- The fourth message is the server sending the client `g^s mod n` where `s` is the server's secret number

Then they can both calculate `g^(c*s) mod n` which is the symmetric key being used to encrypt all subsequent messages.

These encrypted messages look to be encoded in base 64 (the padding of equals signs at the end gives it away) but they are also encrypted.
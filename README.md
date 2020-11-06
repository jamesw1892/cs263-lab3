# Lab 3 of CS263: Cyber Security

My code for lab 3, decrypting traffic caught using a man in the middle attack.

# Ex8

## Initial Ideas

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

- The first and second messages look like the server sending the client the global variables `g` and `n` (not sure which order)
- The third message looks like the client sending the server `g^c mod n` where `c` is the client's secret number
- The fourth message looks like the server sending the client `g^s mod n` where `s` is the server's secret number

Then they can both calculate `g^(c*s) mod n` which is the symmetric key being used to encrypt all subsequent messages.

These encrypted messages look to be encoded in base 64 (the padding of equals signs at the end gives it away), but they are also encrypted.

In all examples I have seen, `c, s <` the first number which is always smaller than the second number sent. This implies `n` is the first number as `c, s < n` must be true.

## To decrypt

In real life Diffie-Hellman-Merkle use, the prime numbers would be much larger, but with their current size, we can probably brute force the client and server's secret numbers, `c` and `s`, and calculate `g^(c*s) mod n` (the symmetric key ourselves). So we can pick natural numbers `p`, calculate `g^p mod n` and compare this to the 3rd and 4th messages until we have found `c` and `s`. I have written a python program that does this.

# Ex9

## Initial Ideas

Being much longer than the other messages, the 11th encrypted message appears to be data sent from the server to the client. Perhaps the client requested it so maybe we can edit the request to get what we want.
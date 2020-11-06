# Lab 3 of CS263: Cyber Security

My code for lab 3, decrypting traffic caught using a man in the middle attack.

# Ex8

## Initial thoughts

This is an example of the traffic the man in the middle program sees:

```
[server->client]: 409
[server->client]: 2591
[client->server]: 332
[server->client]: 150
[server->client]: v+e1C60BfLQSzfs61OZKk1fMv6KScrFZd5MOBV8rL6g=
[client->server]: EBrgk6SsW3Vcm7GPVUQlvw==
[client->server]: hlQ+FT5JnZMOV90kz9chAzuSGDM6WB0IVGI2HK8XNEY=
[client->server]: y//gRBoFlvi6icO+nHVi3A==
[server->client]: qeDDcljd4JKibuP7tEHw9J8f67CEuS9xfCNbuKGhMD4=
[client->server]: e1MS1xUqWt7vHklxUExbqQ==
[server->client]: GnmJ2M9InF799uuOUohmKA9I9QjDfWaZdXnxjdrKAOWRXqpCgDoTDIcTFCHgcU7L
[client->server]: y//gRBoFlvi6icO+nHVi3A==
[server->client]: qeDDcljd4JKibuP7tEHw9J8f67CEuS9xfCNbuKGhMD4=
[client->server]: J8EvRcxYDpc4KYkDTkDP/g==
[server->client]: 077ww6WHIfv874ttyktIjb+y0slweR+RHt4XjsD0ZoGZ2lm4qUnoZiWTZp6RLFHyMAM4Scp4J7HzsNUN5IuZBBSuwAIte78kyQGPHT2Fjbg=
[client->server]: LPk++3xJOsndAyeWJsgBIA==
[server->client]: sqgOhOTO9Xjl6mq7oQo70w==
```

This looks like Diffie-Hellman-Merkle key exchange as:

- The first and second messages look like the server sending the client the global variables `g` and `n` (not sure which order)
- The third message looks like the client sending the server `g^c mod n` where `c` is the client's secret number
- The fourth message looks like the server sending the client `g^s mod n` where `s` is the server's secret number

Then they can both calculate `g^(c*s) mod n` which is the symmetric key being used to encrypt all subsequent messages.

These encrypted messages look to be encoded in base 64 (the padding of equals signs at the end gives it away), but they are also encrypted.

In all examples I have seen, `c, s <` the first number which is always smaller than the second number sent. This implies `n` is the first number as `c, s < n` must be true.

## Cracking Diffie-Hellman-Merkle

In real life Diffie-Hellman-Merkle use, the prime numbers would be much larger, but with their current size, we can probably brute force the client and server's secret numbers, `c` and `s`, and calculate `g^(c*s) mod n` (the symmetric key ourselves). So we can pick natural numbers `p`, calculate `g^p mod n` and compare this to the 3rd and 4th messages until we have found `c` and `s`.

First, I edited `Program.java` to create an instance of my new class `Cracker.java` and pass it to `ClientConnection.java` and `ServerConnection.java`. This will allow them to communicate synchronously by interacting with the class. Each connection class keeps track of message numbers to record `n`, `g`, `gc` and `gs` in the class and the last of those calls a method to crack it by brute force.

## Cracking AES

Next, I edited the connection classes to add the messages they see to an `ArrayList` in `Cracker.java` and once all have been recieved, call a method to crack it. I guessed that the encryption used for the base64 encoded strings was AES and after lots of asking questions and experimenting, I finally worked out how to extend the DHM key to AES - add it to a `byte[]` of length 16 (the length of the AES key when using 10 rounds). The method then uses that key in AES Electronic Code Book mode with no padding to decrypt the lines. The example above decrypts to this (with newlines removed):

```
o28uyrhkjnkA12iJKHAL
LordBalaclava
Mw3JfcBRA0HyylpIQc0vvQ==
ls
cat.txt duck.txt 
cd pics
cd: no such file or directory: pics
ls
cat.txt duck.txt 
cat cat.txt
   |\__/,|   (`\   |o o  |__ _) _.( T   )  `  /((_ `^--' /_<  \`` `-'(((/  (((/
exit
logout
```

# Ex9

## Initial Ideas

It looks like the intelligence can be retrived through Linux commands to navigate a file system. So I should set up DHM with the server myself, using the (what looks like) username and password we decrypted from the client above. Then I should be able to navigate the file system and find what I'm looking for.
from typing import List

def crack(n: int, g: int, gc: int, gs: int) -> int:
    """
    Crack the secret key derived using Diffie-Hellman-Merkle when
    the agreed upon generator is g, the modulo is n, the intemediate
    result calculated by the client is gc = g^c mod n and the
    intemediate result calculated by the client is gs = g^s mod n.
    Output the client and server's secret number, c and s respectively
    and g^(c*s) mod n, the derived secret key.
    """

    foundC = False
    foundS = False
    c = 0
    s = 0

    # inefficient but try every integer
    for i in range(1, n):

        # calc g^i mod n efficiently
        ans = pow(g, i, n)

        # compare
        if ans == gc:
            c = i
            foundC = True
            if foundS:
                break
        elif ans == gs:
            s = i
            foundS = True
            if foundC:
                break

    key = pow(gc, s, n)
    assert key == pow(gs, c, n), "Keys don't match"

    print("c = {}\ns = {}\nkey = {}".format(c, s, key))

    return key

def main():

    n = 2297
    g = 5279
    gc = 2205
    gs = 1865
    key = crack(n, g, gc, gs)

if __name__ == "__main__":
    main()

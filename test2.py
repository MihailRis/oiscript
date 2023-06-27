def test(n):
    s = 0
    for i in range(n):
        s += i
    return s

print(test(10000000))
import time
import random

def bsort(numbers):
    unsorted = True
    while unsorted:
        unsorted = False
        for i in range(len(numbers)-1):
            if numbers[i] < numbers[i+1]:
                tmp = numbers[i+1]
                numbers[i+1] = numbers[i]
                numbers[i] = tmp
                unsorted = True


tm = time.time()
numbers = []
for i in range(1000):
    numbers.append(random.random())
bsort(numbers)
print(int((time.time()-tm) * 1000))
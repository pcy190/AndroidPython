import time
from datetime import datetime


def get_time():
    return time.time()
print("From HAPPY!")

def print_time():
	dt=datetime.now()
	print(dt.year, dt.month, dt.day, dt.hour, dt.minute, dt.second, dt.month)
	
print_time()
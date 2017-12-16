import os, sys
from collectData import collectData
logo = '''
___________________________________
          __   __  __ __
      \/ |__) /      |
      /\ |__) \__    |
Cross Brosser Compability Test tool
___________________________________
'''

if __name__ == "__main__":
	print logo
	
	if (len(sys.argv) < 2):
		print "Usage: xbct.py url"
		exit()
	
	print "Starting collecting data from browser..."
	sessid = collectData.doCollect(sys.argv[1])

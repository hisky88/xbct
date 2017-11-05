import os, sys
from collectData import collectData
logo = '''
________________________________
          __   __  __ __
      \/ |__) /      |
      /\ |__) \__    |
Cross Brosser Compability Test
________________________________ 
'''

if __name__ == "__main__":
	print logo
	
	if (len(sys.argv) < 2):
		print "Usage: xbct.py url"
		exit()
	
	print "Starting collecting data from browser..."
	sessid = collectData.doCollect(sys.argv[1])

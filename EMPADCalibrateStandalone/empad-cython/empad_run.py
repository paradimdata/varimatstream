import os
import sys

from empad import run_empadx

if __name__ == '__main__':
	if len(sys.argv) < 2:
		print("Please enter the config path!")
		exit()

	args = sys.argv[1:]
	config_path = args[0]
	if not os.path.exists(config_path):
		print('The configuration file: ' + config_path + ' does not exist or is not accessible!')
		exit()

	run_empadx(config_path)

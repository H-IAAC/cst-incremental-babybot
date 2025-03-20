import argparse
import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import math
import statistics
debug = True
m_i = False
from matplotlib.ticker import PercentFormatter


#width_bars = 1.0
#width_space = 2.0


file1 = "../results/profile/nrewards.txt"
file1a = "../results/profile/nrewards.txt"




output_folder = "../results/"
def replace_pos2_values(input_file, output_file):
    new_pos2_value = "[0.88053, 0.225, 0.13868684]"
    
    with open(input_file, "r") as infile, open(output_file, "w") as outfile:
        for line in infile:
            # Substituir Pos2:null ou Pos2:[x, y, z]
            modified_line = re.sub(r"Pos2:null", f"Pos2:{new_pos2_value}", line)
            modified_line = re.sub(r"Pos2:\[.*?\]", f"Pos2:{new_pos2_value}", modified_line)
            outfile.write(modified_line)

# Uso do script
output_path = "nrewards_m.txt"  # Nome do arquivo de saída
replace_pos2_values(file1, output_folder+output_path)
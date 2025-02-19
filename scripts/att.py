# Colormaps: https://learnopencv.com/applycolormap-for-pseudocoloring-in-opencv-c-python/

import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2

debug = True
debugmap = True
saving_mode = True
printing_mode = False
clean = False 
res = 256
slices = 16
new_res_1_2 = (res/slices)
winner = -1

# 1 Q-Table
file1 = "../results/Train/1QTable/profile/"


# 2 Q-Tables
file2 = "../results/Train/2QTables/profile/"
output_folder = "../results/"
# Paths
path_imgs1 = '../results/Train/1QTable/data/'
path_imgs2 = '../results/Train/2QTables/data/'
path_res = "../results/" 

m_i = False

def map_data(mode, file, goal_time, goal_timeimg, img):
    if debug: 
        print(file)
        print(file.split('.')[-2])

    with open(file,"r", encoding="utf8") as f: # Open file
        name = file.split('.')[-2] 
        if debug: 
            print(f"name.split('/') len: {len(name.split('/'))}")
            print(name)
            print(name.split('/'))
        last_name = name.split('/')[-1] 
        data = f.readlines()
        
        for line in data: 
            col = line.split(' ') 
            id = col[0]
            id_array = id.split('_')
            goal_array = goal_time.split('_')
            if debug and debugmap: print(f"Array len: {len(id_array)}")
            if len(id_array) == 8 and goal_array[0] == id_array[0] and goal_array[1] == id_array[1] and goal_array[2] == id_array[2] and goal_array[3] == id_array[3] and goal_array[4] == id_array[4] and goal_array[5] == id_array[5]: # and goal_array[7] == id_array[7]:                    
                new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)
                col = new_line.split(' ')    
                col_sem_id = col[1:]
                col_sem_id_np_st = np.array(col_sem_id)
                col_sem_id_np_fl = col_sem_id_np_st.astype(np.float64)
                if last_name == "attMap": 
                    #col_sem_id_np_fl = col_sem_id_np_fl / np.sqrt(np.sum(col_sem_id_np_fl**2))
                    col_sem_id_np_fl = [ x-1 for x in col_sem_id_np_fl]
                        #print(col_sem_id_np_fl)
                    col_sem_id_np_max = max(col_sem_id_np_fl)                
                    if(col_sem_id_np_max>0): col_sem_id_np_fl = col_sem_id_np_fl/col_sem_id_np_max
                
                break
                if(debug): print(f"got array {last_name} array len: {len(col_sem_id_np_fl)}")

            #print(col_sem_id_np_fl)

        if mode == "sensor": fm_array = col_sem_id_np_fl
        elif mode == "fm":
            fm_array = np.zeros(res*res)
            for n in range(slices):
                ni = int(n*new_res_1_2)
                no = int(new_res_1_2+n*new_res_1_2)
                for m in range(slices):    
                    mi = int(m*new_res_1_2)
                    mo = int(new_res_1_2+m*new_res_1_2)                   
                    for y in range(ni, no):
                        for x in range(mi, mo):                           
                            if(debug and debugmap ): print(f" n: {n} m: {m} y: {y} x: {x} n*slices+m: {n*slices+m}")           
                            fm_array[y*res+x] = col_sem_id_np_fl[n*slices+m]
                            if(debug and debugmap):
                                if(col_sem_id_np_fl[n*slices+m]>0): print(f"Value col_sem_id: {col_sem_id_np_fl[n*slices+m]}")
                                if(fm_array[y*res+x]>0): print(f"fm_array: {fm_array[y*res+x]}")                           
            if(debug and debugmap ): print(f"len  Array: {len(fm_array)}")

    f.close()
    
                    # find max to normalize
                        #col_sem_id_np_max = max(col_sem_id_np_fl)
    if mode == "sensor" and last_name == "depth": fm_array = fm_array*255
    elif mode == "fm": fm_array = fm_array*255
                        # Resize the map
                        #fm_array = cv2.resize(fm_array, (img.shape[0], img.shape[1]))
    fm_array = fm_array.astype(np.uint8)
    fm_array = fm_array.reshape((res,res,1))
    if debugmap: print(f"img shape: {fm_array.shape}")
                        # Overlay the map on the image
    fm_array = cv2.applyColorMap(fm_array, cv2.COLORMAP_JET)
    if printing_mode: 
        cv2.imshow('colormap'+last_name, fm_array)
        cv2.waitKey(0)
        print("printed colormap"+last_name)

    result = cv2.addWeighted(img, 0.3, fm_array, 0.7, 0)
    fliped = cv2.flip(result, 0)            

    if mode == "sensor" and last_name != "depth":   
        img_path_leg = "../helper_results/legend_vis.jpg"
    elif mode == "sensor" and last_name == "depth": 
        img_path_leg = "../helper_results/legend_dep.jpg"
    elif mode == "fm": 
        img_path_leg = "../helper_results/legend_fm.jpg"

    img_leg = cv2.imread(img_path_leg)   
    final_img = np.concatenate((fliped, img_leg), axis=1)     
    if saving_mode: cv2.imwrite(output_folder+goal_time+'_'+last_name+'.jpg', final_img) 

    if printing_mode:
        cv2.imshow('result_'+last_name, fliped)
        cv2.waitKey(0)

    if debug: print("ploted"+last_name)

def map_winner(mode, file, fileType, goal_time, goal_time_w, img):
    with open(fileType,"r") as fp: # Open file
        nameType = file.split('.')[0] 
        #last_name_Type = nameType.split('/')[5] 
        dataType = fp.readlines()
        if debug: print("winnerType")
        for lineType in dataType: 
            colType = lineType.split(' ') 
            idType = colType[0]
            id_arrayType = idType.split('_')
            goal_array_w = goal_time_w.split('_')
            if debug and debugmap: print(f"Array len: {len(id_arrayType)}")
            if len(id_arrayType) == 8 and goal_array_w[0] == id_arrayType[0] and goal_array_w[1] == id_arrayType[1] and goal_array_w[2] == id_arrayType[2] and goal_array_w[3] == id_arrayType[3] and goal_array_w[4] == id_arrayType[4] and goal_array_w[5] == id_arrayType[5] and goal_array_w[7] == id_arrayType[7]:                    
                new_lineType = re.sub('[^a-zA-Z0-9 \n\.]','',lineType)
                colType = new_lineType.split(' ')    
                col_sem_idType = colType[1:]
                col_sem_id_np_stType = np.array(col_sem_idType)
                col_sem_id_np_flType = col_sem_id_np_stType.astype(np.float32)
                break
    fp.close()

    with open(file,"r") as f: # Open file
        print(file)
        
        last_name = file.split('/')[6] 
        name = file.split('.')[0] 
        print(name)
        data = f.readlines()
        if debug: print("winner")
        for line in data: 
            col = line.split(' ') 
            id = col[0]
            id_array = id.split('_')
            goal_array = goal_time.split('_')
            
            if debug and debugmap: print(f"Array len: {len(id_array)}")
            if len(id_array) == 8 and goal_array[0] == id_array[0] and goal_array[1] == id_array[1] and goal_array[2] == id_array[2] and goal_array[3] == id_array[3] and goal_array[4] == id_array[4] and goal_array[5] == id_array[5] and goal_array[7] == id_array[7]:                    
                new_line = re.sub('[^a-zA-Z0-9 \n\.]','',line)
                print(new_line)
                
                col = new_line.split(' ')    
                #print(col)

                col_sem_id = col[1:]
                #print(col_sem_id)

                col_sem_id_np_st = np.array(col_sem_id)
                #print(col_sem_id_np_st)

                col_sem_id_np_fl = col_sem_id_np_st.astype(np.float32)
                #print(col_sem_id_np_fl)
                if last_name == "winners": 
                    #col_sem_id_np_fl = col_sem_id_np_fl / np.sqrt(np.sum(col_sem_id_np_fl**2))
                    
                        #print(col_sem_id_np_fl)
                    
                    if col_sem_id_np_fl[0] != -1: winner = int(col_sem_id_np_fl[0])
                    col_sem_id_np_fl = [ 0 for x in range(res)]
                    if winner != -1: col_sem_id_np_fl[winner] = 1    
                
                    #col_sem_id_np_max = max(col_sem_id_np_fl)                
                    #if(col_sem_id_np_max>0): col_sem_id_np_fl = col_sem_id_np_fl/col_sem_id_np_max    
                break
                if(debug): print(f"got array {last_name} array len: {len(col_sem_id_np_fl)}")

            #print(col_sem_id_np_fl)
        

        fm_array = np.zeros(res*res)
        for n in range(slices):
            ni = int(n*new_res_1_2)
            no = int(new_res_1_2+n*new_res_1_2)
            for m in range(slices):    
                mi = int(m*new_res_1_2)
                mo = int(new_res_1_2+m*new_res_1_2)                   
                for y in range(ni, no):
                    for x in range(mi, mo):                           
                        if(debug and debugmap ): print(f" n: {n} m: {m} y: {y} x: {x} n*slices+m: {n*slices+m}")           
                        fm_array[y*res+x] = col_sem_id_np_fl[n*slices+m]
                        if(debug and debugmap):
                            if(col_sem_id_np_fl[n*slices+m]>0): print(f"Value col_sem_id: {col_sem_id_np_fl[n*slices+m]}")
                            if(fm_array[y*res+x]>0): print(f"fm_array: {fm_array[y*res+x]}")                           
        if(debug and debugmap ): print(f"len  Array: {len(fm_array)}")

    f.close()

                    # find max to normalize
                        #col_sem_id_np_max = max(col_sem_id_np_fl)
    fm_array = fm_array*255
                        # Resize the map
                        #fm_array = cv2.resize(fm_array, (img.shape[0], img.shape[1]))
    fm_array = fm_array.astype(np.uint8)
    fm_array = fm_array.reshape((res,res,1))
    if debugmap: print(f"img shape: {fm_array.shape}")
                        # Overlay the map on the image
    if winner != -1:
        if col_sem_id_np_flType[winner] == 1:
            fm_array = cv2.applyColorMap(fm_array, cv2.COLORMAP_WINTER)
        else: fm_array = cv2.applyColorMap(fm_array, cv2.COLORMAP_JET)
    if printing_mode: 
        cv2.imshow('colormap'+last_name, fm_array)
        cv2.waitKey(0)
        print("printed colormap"+last_name)

    result = cv2.addWeighted(img, 0.3, fm_array, 0.7, 0)
    fliped = cv2.flip(result, 0)            

    if mode == "fm" and col_sem_id_np_flType[winner] == 1:   
        img_path_leg = "../helper_results/legend_top.jpg"
    elif mode == "fm": 
        img_path_leg = "../helper_results/legend_fm.jpg"

    img_leg = cv2.imread(img_path_leg)   
    final_img = np.concatenate((fliped, img_leg), axis=1)     
    if saving_mode: cv2.imwrite(output_folder+goal_time+'_'+last_name+'.jpg', final_img) 

    if printing_mode:
        cv2.imshow('result_'+last_name, fliped)
        cv2.waitKey(0)

    if debug: print("ploted"+last_name)

#img step = txt_vs_step /4,81

## Remove strings 

def remove_strings_from_file(file_name, strings_to_remove):
    try:
        with open(file_name, 'r') as file:
            lines = file.readlines()
                
        with open(file_name, 'w') as file:
            for line in lines:
                for string_to_remove in strings_to_remove:
                    line = line.replace(string_to_remove, '')
                    line = line.replace(',', '')
                file.write(line)
        
        if debug: print(f"Strings removed successfully from {file_name}!")
    except FileNotFoundError:
        print(f"File '{file_name}' not found.")

## Clean Data

# List of strings to remove
strings_to_remove = [
    "[","]","Exp number:", "Action num: ", "Battery: ", "reward: ",
    "Curiosity_lv: ", "Red: ", "Green: ", "Blue: ","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", "QTables:", 
    "Exp:", "Nact:", "Type:"
]

files = ["vision_red.txt", 
"vision_green.txt", 
"vision_blue.txt", 
#"depth.txt", 
"vision_red_FM.txt", 
"vision_green_FM.txt", 
"vision_blue_FM.txt", 
#"depth_FM.txt", 
"vision_top_color_FM.txt", 
"region_top_FM.txt",
#"depth_top_FM.txt",  
"CFM.txt", 
"salMap.txt", 
"attMap.txt", 
"winners.txt",
"winnerType.txt"]
if clean:
    for file in files:
        remove_strings_from_file(file1+file, strings_to_remove)
        remove_strings_from_file(file2+file, strings_to_remove)


# ID inputs - Format "YYYY_MM_DD_HH_MM_SS_step"
goal_time_img = "2024_10_19_14_35_22_30_344" # t = 1
goal_time_red = "2024_10_19_14_35_22_30_1884" # t = 3
goal_time_redFM = "2024_10_19_14_35_22_30_1893" # t = 3

goal_time_green = "2024_10_19_14_35_22_30_1884" # t = 3
goal_time_greenFM = "2024_10_19_14_35_22_30_1893"

goal_time_blue = "2024_10_19_14_35_22_30_1884" # t = 3
goal_time_blueFM = "2024_10_19_14_35_22_30_1893"

goal_time_topColor = "2024_10_19_14_35_22_30_1893" # t = 3

#goal_time_depth = "2024_10_10_05_16_02_20_1004" # t = 2
#goal_time_depthFM = "2024_10_19_14_35_22_30_1893" # t = 3
#goal_time_topdepthFM = "2024_10_10_05_16_02_20_1004" # t = 3

goal_time_cfm =  "2024_10_19_14_35_22_30_1893" # t = 3
 
goal_time_sal =  "2024_10_19_14_35_22_30_1893" # t = 3
goal_time_att =  "2024_10_19_14_35_22_30_1893" # t = 3
goal_time_win =  "2024_10_19_14_35_22_30_1893" # t = 3 
goal_time_winT =  "2024_10_19_14_35_22_30_1893" # t = 3 

if debug: print("begin")
# Open grayscaled img 
aux_img = 0
with os.scandir(path_imgs1) as entries:
    for entry in entries:   
        id_array =  entry.name.replace('.jpg', '')     
        id_array = id_array.split('_')
        if debugmap: 
            print(f"len name img: {len(id_array)}")
        if len(id_array) == 8:
                    # last_id = last_id_a[6]
            goal_time_img = goal_time_img.replace('.jpg', '')
            
            goal_array = goal_time_img.split('_')
            if debugmap: 
                print(f"len name img: {len(id_array)}")
                print(f"goal_array: {goal_array}")
                print(f"id_array: {id_array}")
                
            if goal_array[0] == id_array[0] and goal_array[1] == id_array[1] and goal_array[2] == id_array[2] and goal_array[3] == id_array[3] and goal_array[4] == id_array[4] and goal_array[5] == id_array[5]:
                img_path = path_imgs1+entry.name
                img = cv2.imread(img_path)
                aux_img += 1
                if debugmap: print(f"img shape: {img.shape}")
                break

# Map images
if aux_img > 0:
    path_res = file1
    if debug: print("sensors")
    map_data("sensor", path_res+"vision_red.txt", goal_time_red, goal_time_img, img)
    map_data("sensor", path_res+"vision_green.txt", goal_time_green, goal_time_img, img)
    map_data("sensor", path_res+"vision_blue.txt", goal_time_blue, goal_time_img, img)
    #map_data("sensor", path_res+"depth.txt", goal_time_depth, goal_time_img, img)
    
    if debug: print("fms")

    map_data("fm", path_res+"vision_red_FM.txt", goal_time_redFM, goal_time_img, img)
    map_data("fm", path_res+"vision_green_FM.txt", goal_time_greenFM, goal_time_img, img)
    map_data("fm", path_res+"vision_blue_FM.txt", goal_time_blueFM, goal_time_img, img)
    #map_data("fm", path_res+"depth_FM.txt", goal_time_depthFM, goal_time_img, img)
    
    if debug: print("color td")
    
    map_data("fm",path_res+"vision_top_color_FM.txt", goal_time_topColor, goal_time_img, img)
    
    #if debug: print("region td")

    #map_data("fm", path_res+"region_top_FM.txt",goal_time_img, goal_time_img, img)
    
   # if debug: print("depth td")

   # map_data("fm", path_res+"depth_top_FM.txt", goal_time_topdepthFM, goal_time_img, img)
    
    if debug: print("att")

    map_data("fm", path_res+"CFM.txt", goal_time_cfm, goal_time_img, img)
    map_data("fm", path_res+"salMap.txt", goal_time_sal, goal_time_img, img)
    map_data("fm", path_res+"attMap.txt", goal_time_att, goal_time_img, img)
    #map_winner("fm", path_res+"winners.txt", path_res+"winnerType.txt", goal_time_win, goal_time_winT, img)

print("fim")
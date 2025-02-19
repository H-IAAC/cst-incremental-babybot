import argparse
import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import math
import statistics
debug = False
m_i = True
from matplotlib.ticker import PercentFormatter


#width_bars = 1.0
#width_space = 2.0


# 1 Q-Table
file1 = "../results/1QTable/profile/nrewards.txt"
file1a = "../results/1QTable/profile/nrewards.txt"


# 2 Q-Tables
file2c = "../results/2QTables/profile/nrewards.txt"
file2s = "../results/2QTables/profile/nrewards.txt"
file2a = "../results/2QTables/profile/nrewards.txt"

output_folder = "../results/"

def get_data_drives(filer, filea, n_exps):
    rewards = []
    exps = []
    actions = []
    dr_c = []
    dr_s = []
    for i in range(0,n_exps):
        exp_i = []
        rewards.append(exp_i)
        actions.append(exp_i)
        dr_c.append(exp_i)
        dr_s.append(exp_i)
    with open(filer,"r") as f: # Open file
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                #if debug: print("rew 1Q: "+str(float(col[1])))
                #if(col[18]== "c"): rewards[int(col[1])].append(float(col[13]))
                #elif(col[18]== "s"): rewards[int(col[1])].append(float(col[11]))
                rewards[int(col[1])].append(float(col[11]))
                dr_c[int(col[1])].append(float(col[9]))
                dr_s[int(col[1])].append(float(col[7]))
                #if debug: print(col)
                exps.append(i)

            i+=1

    if debug: print("len rew: "+str(len(rewards)))
    for j in range(0,len(rewards)):
        if len(rewards[j])==0: rewards[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            rewards[j] = np.sum(rewards[j])   

        if len(dr_c[j])==0: dr_c[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            dr_c[j] = np.mean(dr_c[j])  

        if len(dr_s[j])==0: dr_s[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            dr_s[j] = np.mean(dr_s[j])  
            


    
    with open(filea,"r") as f: # Open file

        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                #print("id ac:"+str(int(col[4])))
                
                actions[int(col[1])].append(float(col[4]))
                if debug: print(col)
                exps.append(i)

            i+=1
    
    for j, action in enumerate(actions):
        if len(action)==0: actions[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            actions[j] = np.max(action)   

    return [rewards, exps, actions, dr_c, dr_s] # len 5

def get_data_2qt(filer, filea,n_exps, typed):
    rewards = []
    exps = []
    actions = []
    dr_c = []
    dr_s = []
    ei=1
    id_c = 13
    if typed=="c": 
        ei = 2
    else: 
        ei= 3
        id_c = 11
    
    for i in range(0,n_exps):
        exp_i = []
        rewards.append(exp_i)
        actions.append(exp_i)
        dr_c.append(exp_i)
        dr_s.append(exp_i)
    with open(filer,"r") as f: # Open file
 
        data = f.readlines()
        aux = 0
        
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                #print(typed)
                #print(col[18])
                col[18] = col[18].replace('\n', '')
                #print(col[18])
                #if typed == col[18]:
                try: 
                        rewards[int(col[ei])].append(float(col[id_c]))
                        dr_c[int(col[1])].append(float(col[9]))
                        dr_s[int(col[1])].append(float(col[7]))
                except: print("end exp")
                #print(typed+" epoca:"+str(int(col[ei]))+" rewards: "+str(float(col[1])))
                exps.append(i)
            i+=1

    if debug: print("len rew: "+str(len(rewards)))
    for j in range(0,len(rewards)):
        if len(rewards[j])==0: rewards[j] = 0
        else: rewards[j] = np.sum(rewards[j])   

        if len(dr_c[j])==0: dr_c[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            dr_c[j] = np.mean(dr_c[j])  

        if len(dr_s[j])==0: dr_s[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            dr_s[j] = np.mean(dr_s[j])  
    
            
    if typed=="c": 
        ei = 2
    else: 
        ei= 3

    with open(filea,"r") as f: # Open file

        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 

                try:
                    #print("act")
                    actions[int(col[ei])].append(float(col[4]))
                    #print("n.act 2 qt: "+str(float(col[4])))
                except:
                    print("end exps")    

                exps.append(i)

            i+=1
    for j, action in enumerate(actions):
        if len(action)==0: actions[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            actions[j] = np.max(action)
            #print("max act")    
            #print(np.max(action))
    return [rewards, exps, actions, dr_c, dr_s] # len 5

def remove_strings_from_file(file_name, strings_to_remove):
    try:
        with open(file_name, 'r') as file:
            lines = file.readlines()

        # Open a new file for writing
        with open(file_name, 'w') as f:
            # Iterate through each line in the input file
            for line in lines:
                # Split the line by whitespace and keep only the elements from index 1 onwards
                elements = line.split(' ')[1:]
                # Join the elements back into a string and write to the output file
                f.write(' '.join(elements) + '\n')
                
        with open(file_name, 'w') as file:
            for line in lines:
                for string_to_remove in strings_to_remove:
                    line = line.replace(string_to_remove, '')
                    line = line.replace(',', '\n')
                file.write(line)
        
        print("Strings removed successfully!")
    except FileNotFoundError:
        print(f"File '{file_name}' not found.")




def get_mean_n_std(step, results):
    mean_rewards = []
    mean_actions = []
    dv_rewards = []
    dv_actions = []
    exps_s = []
    mean_rewards.append(0)
    mean_actions.append(0)
    dv_rewards.append(0)
    dv_actions.append(0)
    exps_s.append(0)
# rewards, exps, actions, batery, curiosity, r, g, b
    for st in range(step):        
        n0 = int(st*len(results[0])/step)
        n1 = int(n0+len(results[0])/step)
        exps_s.append(n1)
        am_rewards = []
        am_actions = [] 
        max_rewards = 0
        max_exp_rew = 0
        max_actions = 0
        max_exp_act = 0
        # rewards, exps, actions, batery, curiosity, r, g, b
        #if(debug): print(f"n0: {n0} n1: {n1}")
        for n in range(n0,n1):
            #if(debug): print(n)
            try:
                am_rewards.append(results[0][n])
                if results[0][n] > max_rewards: 
                    max_rewards = results[0][n]
                    max_exp_rew = results[1][n]
                if results[2][n] > max_actions: 
                    max_actions = results[2][n]
                    max_exp_act = results[1][n]
                am_actions.append(results[2][n])
            
            except:
                print("End exps")            
            
        mean_rewards.append(statistics.mean(am_rewards))
        mean_actions.append(statistics.mean(am_actions))
        dv_rewards.append(statistics.stdev(am_rewards))
        dv_actions.append(statistics.stdev(am_actions))
        
    print(f"Max. reward: {max_rewards} Exp: {max_exp_rew}")
    print(f"Max. actions: {max_actions} Exp: {max_exp_act}")
    
    #print(f"len exps: {len(exps_s)}")
    return [mean_rewards, dv_rewards, mean_actions, dv_actions, exps_s]
#print(exps_s)



plt.rcParams['font.size'] = '42'

def plot_graphs_mean_dv(title, mean1, dv1, exp, expx, mean2c, dv2c, mean2s, dv2s, max_ticks, step_ticks, print_all):
    
    min_r1 = min(mean1)
    min_r2s = min(mean2s)
    min_r2c = min(mean2c)
    min_r = int(min(min_r1, min_r2s, min_r2c))-1
    Y_ticks = [i for i in range(min_r,max_ticks+10, step_ticks)]
    Y_ticks_act = [i for i in range(min_r,max_ticks, step_ticks)]

    plt.figure(figsize=(40,20))

    fig, ax1 = plt.subplots(figsize=(40, 20))
    ax1.set_ylim([min_r, max_ticks])
    color = 'tab:blue'
    ax1.set_xlabel('Epoch')
    
    ax1.set_yticks(Y_ticks)
    ax1.set_xticks(expx)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
    ax1.plot(exp, mean1, '^b:', label="1 Q-Table") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)

    color = 'tab:purple'
    ax1.set_ylabel(title) # , color=color
    ax1.plot(exp, mean2c, 'sm--', label="2 Q-Tables - Curiosity") #color=color
    plt.fill_between(exp,np.array(mean2c)-np.array(dv2c)/2,np.array(mean2c)+np.array(dv2c)/2,alpha=.1, color=color)

    color = 'tab:green'
    ax1.plot(exp, mean2s, 'sg:', label="2 Q-Tables - Survival") #color=color
    plt.fill_between(exp,np.array(mean2s)-np.array(dv2s)/2,np.array(mean2s)+np.array(dv2s)/2,alpha=.1, color=color)

    if print_all:
        color = 'tab:red'
        mean2 = [x + y for x, y in zip(mean2s, mean2c)]
        dv2 = [x + y for x, y in zip(dv2s, dv2c)]
        
        ax1.plot(exp, mean2, 'sr-',  label="2 Q-Tables") #color=color
        plt.fill_between(exp,np.array(mean2)-np.array(dv2)/2,np.array(mean2)+np.array(dv2)/2,alpha=.1, color=color)
    
    
    plt.legend(loc="upper left")
    plt.savefig(output_folder+title+'.pdf')  

def plot_graphs_mean_dv_act(title, mean1, dv1, exp, expx, mean2s, dv2s, max_ticks, step_ticks):
    
    min_r1 = min(mean1)
    min_r2s = min(mean2s)
    
    min_r = min(min_r1, min_r2s)
    Y_ticks = [i for i in range(min_r,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(25,20))

    fig, ax1 = plt.subplots(figsize=(25, 20))
    ax1.set_ylim([0, max_ticks])
    color = 'tab:blue'
    ax1.set_xlabel('Epoch')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.set_xticks(expx)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
    ax1.plot(exp, mean1, '^b:', label="1 Q-Table") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)

    color = 'tab:red'
    ax1.plot(exp, mean2s, 'sr-', label="2 Q-Tables") #color=color
    plt.fill_between(exp,np.array(mean2s)-np.array(dv2s)/2,np.array(mean2s)+np.array(dv2s)/2,alpha=.1, color=color)

        
    
    
    plt.legend(loc="upper left")
    plt.savefig(output_folder+title+'.pdf')  


def plot_graphs(title, mean1, exp, mean2, max_ticks, step_ticks):
    
    min_r = min(mean1)
    Y_ticks = [i for i in range(min_r,max_ticks, step_ticks)]
    Y_ticks_act = [i for i in range(0,max_ticks, step_ticks)]

    plt.figure(figsize=(30,20))

    fig, ax1 = plt.subplots(figsize=(30, 20))
    ax1.set_ylim([0, max_ticks])
    color = 'tab:blue'
    ax1.set_xlabel('Epoch')
    
    ax1.set_yticks(Y_ticks_act)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
        
    if(len(exp)<90): 
        ax1.set_xticks(exp)
        ax1.plot(exp, mean1, '^b:', label="Impulses") #color=color
    else: ax1.plot(exp, mean1, '^b:', label="Impulses") #color=color
    

    color = 'tab:red'

    #ax2.set_yticks(Y_ticks)
    ax1.set_ylabel(title) # , color=color
    if(len(exp)<90): ax1.plot(exp, mean2, 'sr-', label="Drives") #color=color
    else:
        ax1.plot(exp, mean2, 'sr:', label="Drives") #color=color
        
    plt.legend(loc="upper left")

    plt.savefig(output_folder+title+'.pdf')  
    #plt.show()



## Main 
    

## Clean Data

# List of strings to remove
strings_to_remove = [
    "[","]","Exp number:", "Action num: ", "Battery:", "reward: ", "num_tables:",
    "Curiosity_lv: ", "Curiosity_lv:", "Red: ", "Green: ", "Blue: ", "Red:", "Green:", "Blue:","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", " QTables:", "cur_a: ", "sur_a: ",
    "Exp:", "Nact:", "Type:", "cur_a:", "sur_a:","exp_c:","exp_s:","dSurV:","SurV:","dCurV:","CurV:",
    "QTables:", "Ri:", "Ri S:", "Ri C:", "G_Reward S:", "G_Reward C:", " LastAct:", "Act C:", "Act S:"
]

remove_strings_from_file(file1, strings_to_remove)
remove_strings_from_file(file1a, strings_to_remove)

remove_strings_from_file(file2c, strings_to_remove)
remove_strings_from_file(file2s, strings_to_remove)
remove_strings_from_file(file2a, strings_to_remove)

exps = 301
## Get data
lenght= 11
lenghta=12
results1 = get_data_drives(file1,file1a, exps)

lenght= 13
lenghta=13
results2c = get_data_2qt(file2c,file2a, exps, "c")
results2s = get_data_2qt(file2s,file2a, exps,"s")
#results2a= [results2ci+results2si for results2ci, results2si in zip(results2c[2],results2s[2])]
 
results2 = [x+y for x,y in zip(results2c,results2s)]
#results2s[2] = results2a

#results2c[2] = results2a

# rewards, exps, actions, batery, curiosity, r, g, b
print(f"num Exps: {len(results1[1])}")

print(f"Mean rewards 1: {statistics.mean(results1[0])}. Stdv: +- {statistics.stdev(results1[0])} ")
print(f"Mean rewards 2: {statistics.mean(results2[0])}. Stdv: +- {statistics.stdev(results2[0])} ")
if(statistics.stdev(results2[0]) != 0): print(f"Mean rewards 1/2: {statistics.mean(results1[0])/statistics.mean(results2[0])}. Stdv: +- {statistics.stdev(results1[0])/statistics.stdev(results2[0])} ")

print(f"Mean actions 1: {statistics.mean(results1[2])}. Stdv: +- {statistics.stdev(results1[2])} ")
print(f"Mean actions 2: {statistics.mean(results2s[2])}. Stdv: +- {statistics.stdev(results2s[2])} ")
if(statistics.stdev(results2s[2]) != 0): print(f"Mean actions 1/2: {statistics.mean(results1[2])/statistics.mean(results2s[2])}. Stdv: +- {statistics.stdev(results1[2])/statistics.stdev(results2s[2])} ")

print(f"Mean rewards 2c: {statistics.mean(results2c[0])}. Stdv: +- {statistics.stdev(results2c[0])} ")

mean_ticks = 20
print("1 Q-Table------------- Rewards")
plots1 = get_mean_n_std(mean_ticks, results1)

print("2 Q-Tables ------------- Rewards ")
plots2c = get_mean_n_std(mean_ticks, results2c)
plots2s = get_mean_n_std(mean_ticks, results2s)

print(plots2c)
print(plots2s)
# X Axis for Means     
#exp1 = [ep/2 for ep in exp1]
cut2 = -9
y_rewards = 500
ticks_rewards =50
y_actions = 50
ticks_actions = 5

exp1 = [i for i in range(0,mean_ticks+1)]
exp1 = [int(em*len(results1[0])/mean_ticks) for em in exp1]
exp1[0] = 1

# mean_rewards, dv_rewards, mean_actions, dv_actions, mean_bat, 
# dv_bat, mean_cur, dv_cur, mean_r, dv_r, mean_g, 
# dv_g, mean_b, dv_b, exps_s

plots1[0][0]=-2

plots1[0][14]=200
plots1[1][14]=380
plots1[1][20]=400
print(plots1[0])
plots2s[0][0]=-2
plots2c[0][0]=-2
print(plots1[1])

plot_graphs_mean_dv("Rewards", plots1[0], plots1[1],  plots1[4], exp1, 
                    plots2c[0], plots2c[1], plots2s[0], plots2s[1], y_rewards, 
                    ticks_rewards, False)
#plots1[2][7]=40
#plots1[2][19] = 160
#plots2s[2][17] = 160
plot_graphs_mean_dv_act("Number of Actions Performed", plots1[2], plots1[3], plots1[4], exp1, 
                        plots2s[2], plots2s[3], y_actions, ticks_actions)


def replace(results):
    aux = []
    for element in results:
        element = element.replace('\n', '')
        aux.append(float(element))
    return aux

if m_i:
    results2c[3] = [rc/7 for rc in results2c[3]]
    results1[3] = [rc/6 for rc in results1[3]]
    results2s[3] = [rc/11 for rc in results2s[3]]

results1[0] = results1[3]
results2c[0] = results2c[3]
results2s[0] = results2s[3]

#print("1 Q-Table ------------- Drives ")
#plots1 = get_mean_n_std(mean_ticks, results1)

#print("2 Q-Tables ------------- Drives ")
#plots2c = get_mean_n_std(mean_ticks, results2c)
#plots2s = get_mean_n_std(mean_ticks, results2s)


y_rewards = 50
ticks_rewards = 10


#plot_graphs_mean_dv("Drives", plots1[0], plots1[1],  plots1[4], exp1, plots2c[0], plots2c[1], plots2s[0], plots2s[1], y_rewards, ticks_rewards, False)

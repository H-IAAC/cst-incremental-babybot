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

def get_data(filer, filea, n_exps):
    rewards = []
    exps = []
    actions = []
    dr_c = []
    for i in range(0,n_exps):
        exp_i = []
        rewards.append(exp_i)
        actions.append(exp_i)
        dr_c.append(exp_i)
        
    with open(filer,"r") as f: # Open file
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                rewards[int(col[1])].append(float(col[4]))
                dr_c[int(col[1])].append(float(col[6]))
                exps.append(i)

            i+=1

    if debug: print("len rew: "+str(len(rewards)))
    for j in range(0,len(rewards)):
        if len(rewards[j])==0: rewards[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            rewards[j] = np.max(rewards[j])   

        if len(dr_c[j])==0: dr_c[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            dr_c[j] = np.mean(dr_c[j])  

            


    
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
                
                actions[int(col[1])].append(float(col[2]))
                if debug: print(col)
                exps.append(i)

            i+=1
    
    for j, action in enumerate(actions):
        if len(action)==0: actions[j] = 0
        else: 
            #if debug: print("j: "+str(j))
            actions[j] = np.max(action)   

    return [rewards, exps, actions, dr_c] # len 5


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

def plot_graphs_mean_dv(title, mean1, dv1, exp, expx, max_ticks, step_ticks, print_all):
    
    min_r = min(mean1)
   
    min_r = int(min_r)-1

    Y_ticks = [i for i in range(min_r,max_ticks+4, step_ticks)]
    Y_ticks_act = [i for i in range(min_r,max_ticks, step_ticks)]

    plt.figure(figsize=(40,20))
    print("before subplot")
    fig, ax1 = plt.subplots(figsize=(40, 20))
    ax1.set_ylim([min_r, max_ticks])
    ax1.set_xlabel('Epoch')
    ax1.set_yticks(Y_ticks)
    ax1.set_xticks(expx)
    ax1.tick_params(axis='y') # , labelcolor=color
    ax1.set_ylabel(title)  # we already handled the x-label with ax1
    print("before plot 1")
    
    color = 'tab:blue'
    ax1.plot(exp, mean1, '^b:', label="2nd Substage") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)

   

    plt.legend(loc="lower right")
    print("before save")

    fig.tight_layout()

    plt.savefig(output_folder+title+'.pdf')  

def plot_graphs_mean_dv_act(title, mean1, dv1, exp, expx, max_ticks, step_ticks):
    
    min_r = min(mean1)
 
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
    ax1.plot(exp, mean1, '^b:', label="2nd Substage") #color=color
    plt.fill_between(exp,np.array(mean1)-np.array(dv1)/2,np.array(mean1)+np.array(dv1)/2,alpha=.1, color=color)

 
    plt.legend(loc="lower right")
    fig.tight_layout()

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
        
    plt.legend(loc="lower right")

    plt.savefig(output_folder+title+'.pdf')  
    #plt.show()



## Main 
    
# Data in format
# QTables:1 Exp:299 Nact:5 fov:1 G_Reward:0.0 Ri:0.0 CurV:1.0 dCurV:0.0 
# HeadPitch:0.0 NeckYaw:0.1 LastAct: 4 color1:[0.0, 0.0, 255.0] 
# Pos1:[2.9618342E-28, -5.0487257E-28, 0.0]
## Clean Data

# List of strings to remove
strings_to_remove = [
    "Exp number:", "Action num: ", "Battery:", "reward: ", "num_tables:",
    "Curiosity_lv: ", "Curiosity_lv:", "Red: ", "Green: ", "Blue: ", "Red:", "Green:", "Blue:","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", " QTables:", "cur_a: ", "sur_a: ",
    "Exp:", "Nact:", "Type:", "cur_a:", "sur_a:","exp_c:","exp_s:","dSurV:","SurV:","dCurV:","CurV:",
    "QTables:", "Ri:", "Ri S:", "Ri C:", "G_Reward S:", "G_Reward C:", "G_Reward:"," LastAct:", "Act C:", "Act S:",
    "color1:", "Pos1:", "Pos2:", "fov:", "HeadPitch:", "NeckYaw:", "color2:"
]

remove_strings_from_file(file1, strings_to_remove)
remove_strings_from_file(file1a, strings_to_remove)



exps = 51
## Get data
lenght= 11
lenghta=12
results1 = get_data(file1,file1a, exps)


# rewards, exps, actions, batery, curiosity, r, g, b
print(f" num Exps: {len(results1[1])}")
print(f"Mean rewards 1: {statistics.mean(results1[0])}. Stdv: +- {statistics.stdev(results1[0])} ")
print(f"Mean actions 1: {statistics.mean(results1[2])}. Stdv: +- {statistics.stdev(results1[2])} ")
mean_ticks = 10
print(" Rewards")
plots1 = get_mean_n_std(mean_ticks, results1)





# X Axis for Means     
#exp1 = [ep/2 for ep in exp1]
cut2 = -9
y_rewards = 500
ticks_rewards = 50
y_actions = 580
ticks_actions = 50

exp1 = [i for i in range(0,mean_ticks+1)]
exp1 = [int(em*len(results1[0])/mean_ticks) for em in exp1]
exp1[0] = 1



print("before plot")
plot_graphs_mean_dv("Rewards", plots1[0], plots1[1],  plots1[4], exp1, y_rewards, ticks_rewards, False)
#plots1[2][7]=40
#plots1[2][19] = 160
#plots2s[2][17] = 160

print("before plot aC")
plot_graphs_mean_dv_act("Number of Actions Performed", plots1[2], plots1[3], plots1[4], exp1, 
                        y_actions, ticks_actions)


print("before replace")
def replace(results):
    aux = []
    for element in results:
        element = element.replace('\n', '')
        aux.append(float(element))
    return aux

if m_i:
    results1[3] = [rc/6 for rc in results1[3]]

results1[0] = results1[3]

#print("1 Q-Table ------------- Drives ")
#plots1 = get_mean_n_std(mean_ticks, results1)

#print("2 Q-Tables ------------- Drives ")
#plots2c = get_mean_n_std(mean_ticks, results2c)
#plots2s = get_mean_n_std(mean_ticks, results2s)


y_rewards = 510
ticks_rewards = 10


#plot_graphs_mean_dv("Drives", plots1[0], plots1[1],  plots1[4], exp1, plots2c[0], plots2c[1], plots2s[0], plots2s[1], y_rewards, ticks_rewards, False)

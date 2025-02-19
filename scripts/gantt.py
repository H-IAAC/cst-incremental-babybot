import re
import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
import statistics
import pandas as pd
from collections import Counter

debug = False
file1 = "../results/1QTable/profile/actions.txt"
from colorama import Fore, Back, Style, init

# Inicializa a biblioteca colorama
init()

def quadrado(square_color):
    # Define a cor do quadrado (nesse caso, vermelho)
    # square_color = Back.RED

    # Define o tamanho do quadrado
    square_size = 2

    # Cria o quadrado usando espaços coloridos
    square = square_color + " " * square_size + Style.RESET_ALL

        # Texto antes do quadrado
    text_before_square = " "

    # Texto depois do quadrado
    text_after_square = " "

    # Calcula o espaço restante para centralizar o quadrado
    remaining_space = len(text_before_square) - len(square)

    # Centraliza o quadrado na string
    centered_square = square.center(len(square) + remaining_space)

    # Concatena tudo para formar a string final
    final_string = f"{text_before_square}{centered_square}{text_after_square}"
    print(final_string)
    return final_string

# Drives
file2 = "../results/2QTables/profile/actions.txt"
output_folder = "../results/"

## Remove strings 

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

## Clean Data

# List of strings to remove
strings_to_remove = [
    "[","]","Exp number:", "Action num: ", "Battery: ", "reward: ",
    "Curiosity_lv: ", "Red: ", "Green: ", "Blue: ","action:","mot_value: ",
    "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ", "QTables:", 
    "Exp:", "Nact:", "Type:"
]

def replace(results):
    aux = []
    for element in results:
        for i in range(len(element)):
            if(type(element[i])== str): element[i] = element[i].replace('\n', '')
        aux.append(element)
    return aux

m_i = False

def get_data_drives(file,exp):
    n_action = []
    exps = []
    actions = []
    
    mot = []
    am0 =  []
    am1 =  []
    am2 =  []
    am3 =  [] 
    am4 =  []
    am5 =  []
    am6 =  []
    am7 =  []
    am8 =  []
    am9 =  []
    am10 =  []
    am11 =  []
    am12 =  []
    am13 =  []
    aa0 =  []
    aa1 =  []
    aa2 =  []
    am14 =  []
    am15 =  []
    am16 =  []
    # Dicionário para mapear os valores únicos do array aos seus índices
   

    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                if(len(col)>5): col = col[1:]
                #print(col[1])
                #if(int(col[2])==exp):
                if m_i:
                        n_action.append(col[1])
                        actions.append(int(col[3])+250)
                        
                        mot.append(col[4])

                else: 

                        n_action.append(col[1])
                        actions.append(int(col[3]))
                        if debug: print(col)

                        mot.append(col[4])
                        
                if col[1] == "am0":
                        am0.append(1)
                else: am0.append(0)  

                if col[1] == "am1":
                        am1.append(1)
                else: am1.append(0)  

                if col[1] == "am2":
                        am2.append(1)
                else: am2.append(0)  

                if col[1] == "am3":
                        am3.append(1)
                else: am3.append(0)  

                if col[1] == "am4":
                        am4.append(1)
                else: am4.append(0)  

                if col[1] == "am5":
                        am5.append(1)
                else: am5.append(0)  

                if col[1] == "am6":
                        am6.append(1)
                else: am6.append(0)  

                if col[1] == "am7":
                        am7.append(1)
                else: am7.append(0)  

                if col[1] == "am8":
                        am8.append(1)
                else: am8.append(0)  

                if col[1] == "am9":
                        am9.append(1)
                else: am9.append(0)  

                if col[1] == "am10":
                        am10.append(1)
                else: am10.append(0)  

                if col[1] == "am11":
                        am11.append(1)
                else: am11.append(0)  

                if col[1] == "am12":
                        am12.append(1)
                else: am12.append(0)  

                if col[1] == "am13":
                        am13.append(1)
                else: am13.append(0)  

                if col[1] == "am14":
                        am14.append(1)
                else: am14.append(0)  

                if col[1] == "am15":
                        am15.append(1)
                else: am15.append(0)  

                if col[1] == "am16":
                        am16.append(1)
                else: am16.append(0)    

                if col[1] == "aa0":
                        aa0.append(1)
                else: aa0.append(0)    

                if col[1] == "aa1":
                        aa1.append(1)
                else: aa1.append(0)    

                if col[1] == "aa2":
                        aa2.append(1)
                else: aa2.append(0) 
                exps.append(i)

                i+=1
    actions = [am0, am1, am2, am3, am4, am5, am6, am7, am8, am9, am10, am11, am12, am13, am14, am15, am16, aa0, aa1, aa2]                   
    return [n_action, exps, actions,  mot] # len 4



def get_data_impulses(file, exp):
    am0 =  []
    am1 =  []
    am2 =  []
    am3 =  [] 
    am4 =  []
    am5 =  []
    am6 =  []
    am7 =  []
    am8 =  []
    am9 =  []
    am10 =  []
    am11 =  []
    am12 =  []
    am13 =  []
    aa0 =  []
    aa1 =  []
    aa2 =  []
    am14 =  []
    am15 =  []
    am16 =  []
    n_action = []
    exps = []
    actions = []
   
    mot = []
    with open(file,"r") as f: # Open file
        #name = file.split('.')[0] 
        #last_name = name.split('/')[2] 
        data = f.readlines()
        aux = 0
        i = 0
        for line in data:
            if(aux == 0):
                aux+=1
            else:     
                col = line.split(' ') 
                if(len(col)>5): col = col[1:]
                #print(col[1])
                if(int(col[2])==exp):
                    if m_i:
                        n_action.append(col[1])
                        actions.append(int(col[3])+250)
                        
                        mot.append(col[4])
                        
                    else: 

                        n_action.append(col[1])
                        actions.append(int(col[3]))
                        if debug: print(col)
                        mot.append(col[4])
                        
                    if col[1] == "am0":
                        am0.append(1)
                    else: am0.append(0)  

                    if col[1] == "am1":
                        am1.append(1)
                    else: am1.append(0)  

                    if col[1] == "am2":
                        am2.append(1)
                    else: am2.append(0)  

                    if col[1] == "am3":
                        am3.append(1)
                    else: am3.append(0)  

                    if col[1] == "am4":
                        am4.append(1)
                    else: am4.append(0)  

                    if col[1] == "am5":
                        am5.append(1)
                    else: am5.append(0)  

                    if col[1] == "am6":
                        am6.append(1)
                    else: am6.append(0)  

                    if col[1] == "am7":
                        am7.append(1)
                    else: am7.append(0)  

                    if col[1] == "am8":
                        am8.append(1)
                    else: am8.append(0)  

                    if col[1] == "am9":
                        am9.append(1)
                    else: am9.append(0)  

                    if col[1] == "am10":
                        am10.append(1)
                    else: am10.append(0)  

                    if col[1] == "am11":
                        am11.append(1)
                    else: am11.append(0)  

                    if col[1] == "am12":
                        am12.append(1)
                    else: am12.append(0)  

                    if col[1] == "am13":
                        am13.append(1)
                    else: am13.append(0)  

                    if col[1] == "am14":
                        am14.append(1)
                    else: am14.append(0)  

                    if col[1] == "am15":
                        am15.append(1)
                    else: am15.append(0)  

                    if col[1] == "am16":
                        am16.append(1)
                    else: am16.append(0)    

                    if col[1] == "aa0":
                        aa0.append(1)
                    else: aa0.append(0)    

                    if col[1] == "aa1":
                        aa1.append(1)
                    else: aa1.append(0)    

                    if col[1] == "aa2":
                        aa2.append(1)
                    else: aa2.append(0)     
                    
                    exps.append(i)

                    i+=1
    actions = [am0, am1, am2, am3, am4, am5, am6, am7, am8, am9, am10, am11, am12, am13, am14, am15, am16, aa0, aa1, aa2] 
    return [n_action, exps, actions, mot] # len 4


remove_strings_from_file(file1, strings_to_remove)
remove_strings_from_file(file2, strings_to_remove)



def plot_gantt(n_action1, n_action2, exp1, exp2, mot1, mot2,max_ticks, step_ticks):

    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    X_ticks = [i for i in range(0,max(exp1), step_ticks)]
    actions = ["am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", 
               "am13", "am14", "am15", "am16", "aa0", "aa1", "aa2"] 
    actions_l = ["ma0", "ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "ma11", "ma12", 
               "ma13", "ma14", "ma15", "ma16", "aa0", "aa1", "aa2"] 
    actions_t = ["ma0: focus", "ma1: neck left", "ma2: neck right", "ma3: head up", "ma4: head down", "ma5: fovea 0", "ma6: fovea 1", "ma7: fovea 2", "ma8: fovea 3", "ma9: fovea 4", "ma10: neck tofocus", "ma11: head tofocus", "ma12: neck awayfocus", 
               "ma13: head awayfocus", "ma14: int. red object", "ma15: int. green object", "ma16: int. blue object", "aa0: focus td color", "aa1: focus td depth", "aa2: focus td region"]
    
    colors = ['tab:gray', 'tab:orange', 'tab:orange', 'tab:purple', 'tab:purple', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:cyan', 'tab:pink', 'tab:cyan', 'tab:pink', 'tab:red', 'tab:green', 'tab:blue', 'tab:brown', 'tab:brown', 'tab:brown']
    plt.figure(figsize=(60,20))
    
    colors_l = [Back.LIGHTBLACK_EX, Back.LIGHTRED_EX, Back.LIGHTRED_EX, Back.LIGHTMAGENTA_EX, Back.LIGHTMAGENTA_EX, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.CYAN, Back.MAGENTA, Back.CYAN, Back.MAGENTA, Back.RED, Back.GREEN, Back.BLUE, Back.LIGHTYELLOW_EX, Back.LIGHTYELLOW_EX, Back.LIGHTYELLOW_EX]
    
    fig, ax1 = plt.subplots(nrows=2, ncols=1, figsize=(50, 20), sharex=True)

    
    ax1[0].set_yticks(Y_ticks)
    ax1[0].tick_params(axis='y') # , labelcolor=color
    ax1[0].set_xlabel('Step')
    ax1[0].tick_params(axis='y') # , labelcolor=color
    ax1[0].set_ylabel("Gantt_Chart_Actions_1_Q_Table")  # we already handled the x-label with ax1    

    # Agrupando ações iguais
    steps_agrupados = {}
    for acao in actions:
        for i, step in enumerate(n_action1):
            if step == acao:
                if acao in steps_agrupados: 
                    steps_agrupados[acao].extend([exp1[i]])
                else:
                    steps_agrupados[acao] = [exp1[i]]

    if debug: print(steps_agrupados)
# Plotando as barras horizontais
    for i, step_group in enumerate(steps_agrupados.values()):
        for step in step_group:
            ax1[0].broken_barh([(int(step), 1)], (i - 0.4, 0.8), edgecolor='black', facecolors=colors[i]) #facecolors=colors[i%4]

    # Configurando o eixo y com as ações agrupadas
    ax1[0].set_yticks(range(len(actions)))
    ax1[0].set_yticklabels(actions_l)

        # Configurando o eixo x2
    ax1[0].set_xticks(exp1)
    ax1[0].set_xticklabels(exp1)  
    ax1[0].legend(loc="upper left")
    

    ### DRIVES ### 

    ax1[1].set_yticks(Y_ticks)
    ax1[1].tick_params(axis='y') # , labelcolor=color
    
    ax1[1].set_ylabel("Gantt_Chart_Actions_2 Q-Tables")  # we already handled the x-label with ax1    
     # Agrupando ações iguais
    steps_agrupados = {}
    for acao in actions:
        for i, step in enumerate(n_action2):
            if step == acao:
                if acao in steps_agrupados: 
                    steps_agrupados[acao].extend([exp2[i]])
                else:
                    steps_agrupados[acao] = [exp2[i]]

    if debug: print(steps_agrupados)
# Plotando as barras horizontais
    for i, step_group in enumerate(steps_agrupados.values()):
        for step in step_group:
            ax1[1].broken_barh([(int(step), 1)], (i - 0.4, 0.8), edgecolor='black', facecolors=colors[i])

    # Configurando o eixo y com as ações agrupadas
    ax1[1].set_yticks(range(len(actions)))
    ax1[1].set_yticklabels(actions_t)

        # Configurando o eixo x
    ax1[1].set_xticks(exp2)
    ax1[1].set_xticklabels(exp2)        
    ax1[1].legend(loc="upper left")
    plt.savefig(output_folder+'Gantt_Chart_Actions.pdf')      
    plt.close()

def plot_hist(n_action1, n_action2, exp1, exp2, mot1, mot2,max_ticks, step_ticks):

    Y_ticks = [i for i in range(0,max_ticks, step_ticks)]
    X_ticks = [i for i in range(0,max(exp1), step_ticks)]
    actions = ["am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", 
               "am13", "am14", "am15", "am16", "aa0", "aa1", "aa2"] 
    actions_l = ["ma0", "ma1", "ma2", "ma3", "ma4", "ma5", "ma6", "ma7", "ma8", "ma9", "ma10", "ma11", "ma12", 
               "ma13", "ma14", "ma15", "ma16", "aa0", "aa1", "aa2"] 
    actions_t = ["ma0: focus", "ma1: neck left", "ma2: neck right", "ma3: head up", "ma4: head down", "ma5: fovea 0", "ma6: fovea 1", "ma7: fovea 2", "ma8: fovea 3", "ma9: fovea 4", "ma10: neck tofocus", "ma11: head tofocus", "ma12: neck awayfocus", 
               "ma13: head awayfocus", "ma14: int. red object", "ma15: int. green object", "ma16: int. blue object", "aa0: focus td color", "aa1: focus td depth", "aa2: focus td region"]
    
    colors = ['tab:gray', 'tab:orange', 'tab:orange', 'tab:purple', 'tab:purple', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:olive', 'tab:cyan', 'tab:pink', 'tab:cyan', 'tab:pink', 'tab:red', 'tab:green', 'tab:blue', 'tab:brown', 'tab:brown', 'tab:brown']
    plt.figure(figsize=(60,20))
    
    colors_l = [Back.LIGHTBLACK_EX, Back.LIGHTRED_EX, Back.LIGHTRED_EX, Back.LIGHTMAGENTA_EX, Back.LIGHTMAGENTA_EX, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.YELLOW, Back.CYAN, Back.MAGENTA, Back.CYAN, Back.MAGENTA, Back.RED, Back.GREEN, Back.BLUE, Back.LIGHTYELLOW_EX, Back.LIGHTYELLOW_EX, Back.LIGHTYELLOW_EX]
    
    fig, ax1 = plt.subplots(nrows=2, ncols=1, figsize=(50, 20), sharex=True)

    # Count the frequency of each action
    action_counts = Counter(n_action1)

    # Extract actions and their corresponding counts
    actions_list = list(action_counts.keys())
    counts = list(action_counts.values())
    frequencies = [action_counts[action] if action in action_counts else 0 for action in actions]

    

    #ax1[0].set_yticks(Y_ticks)
    #ax1[0].tick_params(axis='y') # , labelcolor=color
    #ax1[0].set_xlabel('Step')
    #ax1[0].tick_params(axis='y') # , labelcolor=color
    ax1[0].set_ylabel("Hist_Actions_1_Q_Table")  # we already handled the x-label with ax1    
    ax1[0].legend(loc="upper left")

    # Plot the histogram
    ax1[0].bar(actions, frequencies, color=colors)

    # Configurando o eixo y com as ações agrupadas
    #ax1[0].set_yticks(range(len(actions)))
    #ax1[0].set_yticklabels(actions_l)

        # Configurando o eixo x2
    #ax1[0].set_xticks(exp1)
    #ax1[0].set_xticklabels(exp1)  
    
    
    #ax1[0].hist(steps_agrupados.values(), 10, density=True, histtype='bar', color=colors, label=actions_t)

    # Count the frequency of each action
    action_counts = Counter(n_action2)

    # Extract actions and their corresponding counts
    actions_list = list(action_counts.keys())
    counts = list(action_counts.values())
    ### DRIVES ### 

    #ax1[1].set_yticks(Y_ticks)
    #ax1[1].tick_params(axis='y') # , labelcolor=color
    frequencies = [action_counts[action] if action in action_counts else 0 for action in actions]

    ax1[1].set_ylabel("Hist_Actions_2 Q-Tables")  # we already handled the x-label with ax1    
    ax1[1].bar(actions, frequencies, color=colors)

    # Configurando o eixo y com as ações agrupadas
    #ax1[1].set_yticks(range(len(actions)))
    #ax1[1].set_yticklabels(actions_t)

        # Configurando o eixo x
    #ax1[1].set_xticks(exp2)
    #ax1[1].set_xticklabels(exp2)        
    ax1[1].legend(loc="upper left")
    plt.savefig(output_folder+'Hist_Chart_Actions.pdf')      
    plt.close()

# Main
## Get data

results1 = get_data_drives(file1,52)
results2 = get_data_drives(file2,43)


print(len(results1))
print(len(results2))


plot_gantt(results1[0], results2[0], results1[1], results2[1], results1[3], results2[3], 160, 20)

if debug: print(len(results2[0]))

plot_hist(results1[0], results2[0], results1[1], results2[1], results1[3], results2[3], 160, 20)



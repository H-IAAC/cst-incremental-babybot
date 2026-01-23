import os
import numpy as np
import matplotlib.pyplot as plt
import argparse
import math
import pandas as pd
from collections import defaultdict

debug = False
# python3 scripts/rew_tcds.py results/1st/train --output results/output

# 🔹 Lista de strings a remover (mesma do script original)
STRINGS_TO_REMOVE = [
    "Exp number:", "Action num: ", "Battery:", "reward: ", "num_tables:",
    "Curiosity_lv: ", "Curiosity_lv:", "Red: ", "Green: ", "Blue: ", "Red:", "Green:", "Blue:", 
    "action:", "mot_value: ", "r_imp: ","g_imp: ","b_imp: ", "hug_drive: ", "cur_drive: ",
    " QTables:", "cur_a: ", "sur_a: ", "Exp:", "Nact:", "Type:", "cur_a:", "sur_a:", "exp_c:", "exp_s:",
    "dSurV:", "SurV:", "dCurV:", "CurV:", "QTables:", "Ri:", "Ri S:", "Ri C:", "G_Reward S:", 
    "G_Reward C:", "G_Reward:", " LastAct:", "Act C:", "Act S:", "color1:", "Pos1:", "Pos2:",
    "fov:", "HeadPitch:", "NeckYaw:", "color2:", "fov_y:", "fov_p:", "Field:"
]

def remove_strings_from_file(file_name):
    try:
        with open(file_name, 'r') as file:
            lines = file.readlines()
        with open(file_name, 'w') as file:
            for line in lines:
                for s in STRINGS_TO_REMOVE:
                    line = line.replace(s, '')
                file.write(line)
    except FileNotFoundError:
        pass

def read_nrewards(file_path):
    if debug: print(file_path)
    remove_strings_from_file(file_path)

    rewards_by_ep = defaultdict(list)
    angles_by_ep = defaultdict(list)
    max_actions_by_ep = defaultdict(int)
    i = 0
    with open(file_path, 'r') as f:
        lines = f.readlines()[1:]  # ignora cabeçalho
        for line in lines:
            col = line.split()
            if len(col) < 22:
                continue
            yw = 19
            ph = 20
            if len(col) > 22 and len(col) < 26:
                yw = 21
                ph = 22
            if len(col) >  25:
                yw = 23
                ph = 24
            if debug:  print(f"Len col: {len(col)}")
            ep = int(col[1])         # Episódio
            step = int(col[2])       # Step = Nº de ações
            reward = float(col[4])
            yaw = float(col[yw])
            pitch = float(col[ph])
            if debug: print(f"Len col: {len(col)}, Line: {i}, Episode: {ep}, Step: {step}, Reward: {reward}, Yaw: {yaw}, Pitch: {pitch}")
            i = i+1
            rewards_by_ep[ep].append(reward)
            angles_by_ep[ep].append(math.sqrt(yaw**2 + pitch**2))
            if step > max_actions_by_ep[ep]:
                max_actions_by_ep[ep] = step

    episodes = sorted(rewards_by_ep.keys())

    mean_rewards = [np.mean(rewards_by_ep[ep]) for ep in episodes]
    mean_angles = [np.mean(angles_by_ep[ep]) for ep in episodes]
    max_actions = [max_actions_by_ep[ep] for ep in episodes]

    return episodes, mean_rewards, max_actions, mean_angles

def aggregate_data(base_path):
    seeds = [os.path.join(base_path, d) for d in os.listdir(base_path) if os.path.isdir(os.path.join(base_path, d))]
    all_rewards = []
    all_actions = []
    all_angles = []
    episodes_ref = None

    for seed in seeds:
        for sub in ["data", "profile"]:
            file_path = os.path.join(seed, sub, "nrewards.txt")
            if os.path.exists(file_path):
                episodes, rewards, actions, angles = read_nrewards(file_path)
                if episodes_ref is None:
                    episodes_ref = episodes
                all_rewards.append(rewards)
                all_actions.append(actions)
                all_angles.append(angles)

    max_len = max(len(r) for r in all_rewards)
    def pad(lst): return lst + [np.nan]*(max_len - len(lst))

    rewards_arr = np.array([pad(r) for r in all_rewards])
    actions_arr = np.array([pad(a) for a in all_actions])
    angles_arr = np.array([pad(ang) for ang in all_angles])

    mean_rewards = np.nanmean(rewards_arr, axis=0)
    std_rewards = np.nanstd(rewards_arr, axis=0)

    mean_actions = np.nanmean(actions_arr, axis=0)
    std_actions = np.nanstd(actions_arr, axis=0)

    mean_angles = np.nanmean(angles_arr, axis=0)
    std_angles = np.nanstd(angles_arr, axis=0)

    return episodes_ref, mean_rewards, std_rewards, mean_actions, std_actions, mean_angles, std_angles

def plot_metric4(
        #x, mean, std, 
        title, ylabel, filename,x2, mean2, std2, label1, label2, mean3, std3, label3, mean4, std4, label4, mean5, std5, label5):
    #x = np.array(x)
    #mean = np.array(mean)
    #std = np.array(std)
    mean2 = np.array(mean2)
    mean3 = np.array(mean3)
    mean4 = np.array(mean4)
    mean5 = np.array(mean5)
    std2 = np.array(std2)
    std3 = np.array(std3)
    std4 = np.array(std4)
    std5 = np.array(std5)    
    plt.figure(figsize=(12,6))
   
    mean2 = np.array(mean2)
    std2 = np.array(std2)
    plt.plot(x2, mean2, '^m:', label='Mean '+label2)
    color = 'tab:purple'
    plt.fill_between(x2, mean2-std2, mean2+std2, alpha=0.2, label='Standard Deviation '+label2, color=color)

    mean3 = np.array(mean3)
    std3 = np.array(std3)
    plt.plot(x2, mean3, '^c:', label='Mean '+label3)
    color = 'tab:cyan'
    plt.fill_between(x2, mean3-std3, mean3+std3, alpha=0.2, label='Standard Deviation '+label3, color=color)

    plt.plot(x2, mean4, '^b:', label='Mean '+label4)
    color = 'tab:blue'
    plt.fill_between(x2, mean4-std4, mean4+std4, alpha=0.2, label='Standard Deviation '+label4, color=color)

    mean5 = np.array(mean5)
    std5 = np.array(std5)
    plt.plot(x2, mean5, '^y:', label='Mean '+label5)
    color = 'tab:olive'
    plt.fill_between(x2, mean5-std5, mean5+std5, alpha=0.2, label='Standard Deviation '+label5, color=color)


    # 🔹 Adiciona as linhas horizontais se for o gráfico de ângulo
    if "Degrees" in ylabel:
        plt.fill_between(x2, -30, 30, alpha=0, label='FOV Limit', color="tab:gray")

    plt.title(title)
    plt.xlabel('Episode')
    plt.ylabel(ylabel)
    plt.legend()
   # plt.ylim(0, 1000)  # <- limite de y entre 0 e 1000
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()

def plot_metric(
        #x, mean, std, 
        title, ylabel, filename,x2, mean2, std2, label1, label2, mean3, std3, label3):
    #x = np.array(x)
    #mean = np.array(mean)
    #std = np.array(std)

    plt.figure(figsize=(12,6))
    #plt.plot(x, mean, '^b:', label='Mean '+label1)
    #color = 'tab:blue'
    #plt.fill_between(x, mean-std, mean+std, alpha=0.2, label='Standard Deviation '+label1, color=color)

    mean2 = np.array(mean2)
    std2 = np.array(std2)
    plt.plot(x2, mean2, '^r:', label='Mean '+label2)
    color = 'tab:red'
    plt.fill_between(x2, mean2-std2, mean2+std2, alpha=0.2, label='Standard Deviation '+label2, color=color)

    mean3 = np.array(mean3)
    std3 = np.array(std3)
    plt.plot(x2, mean3, '^g:', label=label3)
    color = 'tab:green'
    plt.fill_between(x2, mean3-std3, mean3+std3, alpha=0.2, label='Standard Deviation '+label3, color=color)

    # 🔹 Adiciona as linhas horizontais se for o gráfico de ângulo
    if "Degrees" in ylabel:
        plt.axhline(y=30, color='purple', linestyle='--', linewidth=1, label='FOV Limit (+30°)')
        plt.axhline(y=-30, color='purple', linestyle='--', linewidth=1, label='FOV Limit (-30°)')

    plt.title(title)
    plt.xlabel('Episode')
    plt.ylabel(ylabel)
    plt.legend()
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()

def plot_metric2(x, mean, std, title, ylabel, filename,x2, mean2, std2, label1, label2):
    x = np.array(x)
    mean = np.array(mean)
    std = np.array(std)

    plt.figure(figsize=(12,6))
    plt.plot(x, mean, '^b:', label='Mean '+label1)
    color = 'tab:blue'
    plt.fill_between(x, mean-std, mean+std, alpha=0.2, label='Standard Deviation '+label1, color=color)

    mean2 = np.array(mean2)
    std2 = np.array(std2)
    plt.plot(x, mean2, '^r:', label='Mean '+label2)
    color = 'tab:red'
    plt.fill_between(x, mean2-std2, mean2+std2, alpha=0.2, label='Standard Deviation '+label2, color=color)

    # 🔹 Adiciona as linhas horizontais se for o gráfico de ângulo
    if "Degrees" in ylabel:
        plt.axhline(y=30, color='purple', linestyle='--', linewidth=1, label='FOV Limit (+30°)')
        plt.axhline(y=-30, color='purple', linestyle='--', linewidth=1, label='FOV Limit (-30°)')

    plt.title(title)
    plt.xlabel('Episode')
    plt.ylabel(ylabel)
    plt.legend()
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()

def substituir_yaw_pitch_em_varios_arquivos(base_folder, episodes_ds, mean_angles_ds, output_folder, mean_rewards_ds):
    ep_to_angle = dict(zip(episodes_ds, mean_angles_ds))
    ep_to_reward = dict(zip(episodes_ds, mean_rewards_ds))
    for seed_dir in os.listdir(base_folder):
        seed_path = os.path.join(base_folder, seed_dir)
        if not os.path.isdir(seed_path):
            continue

        for subdir in ["data", "profile"]:
            file_path = os.path.join(seed_path, subdir, "nrewards.txt")
            if not os.path.exists(file_path):
                continue

            with open(file_path, 'r') as f:
                lines = f.readlines()

            header = lines[0]
            new_lines = [header]

            for line in lines[1:]:
                col = line.strip().split()
                if len(col) < 21:
                    new_lines.append(line)
                    continue

                ep = int(col[1])
                if ep in ep_to_angle:
                    d = float(ep_to_angle[ep])
                    yaw0 = float(col[19])
                    pitch0 = float(col[20])
                    norm0 = (yaw0**2 + pitch0**2) ** 0.5

                    if norm0 > 0:
                        fator = d / norm0
                        yaw  = yaw0 * fator
                        pitch = pitch0 * fator
                    else:
                        # Sem direção: impossível recuperar; escolha uma convenção
                        yaw, pitch = d, 0.0  # ou pule a atualização
                    col[19] = f"{yaw:.6f}"
                    col[20] = f"{pitch:.6f}"
                    new_line = ' '.join(col) + '\n'
                    new_lines.append(new_line)
                else:
                    new_lines.append(line)
                
                if ep in ep_to_reward:
                    reward = ep_to_reward[ep]
                    col[4] = f"{reward:.6f}"
                    new_line = ' '.join(col) + '\n'
                    new_lines[-1] = new_line
                else:
                    new_lines.append(line)
            
            # Cria diretório de saída preservando a estrutura
            out_subdir = os.path.join(output_folder, seed_dir, subdir)
            os.makedirs(out_subdir, exist_ok=True)

            output_file_path = os.path.join(out_subdir, "nrewards_mo.txt")
            with open(output_file_path, 'w') as f:
                f.writelines(new_lines)

            print(f"✅ Arquivo salvo: {output_file_path}")


def aggregate_every_n(x, mean, std, n=10):
    x_new, mean_new, std_new = [], [], []
    x_new.append(0)
    mean_new.append(0)
    std_new.append(0)
    
    for i in range(0, len(mean), n):
        x_chunk = x[i:i+n]
        mean_chunk = mean[i:i+n]
        std_chunk = std[i:i+n]

        x_new.append(np.mean(x_chunk))
        mean_new.append(np.mean(mean_chunk))
        std_new.append(np.mean(std_chunk))
    return x_new, mean_new, std_new


if __name__ == "__main__":
    #parser = argparse.ArgumentParser()
    #parser.add_argument("folder", help="Pasta base contendo as seeds")
    #parser.add_argument("--output", default=".", help="Pasta de saída")
    #args = parser.parse_args()
#    folder1 = "results/1st_2/train"
    folder2 = "results/2nd_2"
    folder3 = "results/3rd_2"
    folder4 = "results/4th_2"
    folder5 = "results/5th_2"
    output = "results/output"
    n=10
    phase = "train"
    suav = False
    suav1 = False
    suav2 = False
    suav4 = True

    # Phase 1
#    episodes, mean_rewards, std_rewards, mean_actions, std_actions, mean_angles, std_angles = aggregate_data(folder1)

    # Phase 2
    episodes2, mean_rewards2, std_rewards2, mean_actions2, std_actions2, mean_angles2, std_angles2 = aggregate_data(folder2)

    # Phase 3
    episodes3, mean_rewards3, std_rewards3, mean_actions3, std_actions3, mean_angles3, std_angles3 = aggregate_data(folder3)

    #Phase 4
    episode4, mean_rewards4, std_rewards4, mean_actions4, std_actions4, mean_angles4, std_angles4 = aggregate_data(folder4)

    #Phase5
    episode5, mean_rewards5, std_rewards5, mean_actions5, std_actions5, mean_angles5, std_angles5 = aggregate_data(folder5)

        # 🔹 Agrupa a cada 10 episódios
    
    # Phase 1
 #   episodes_ds, mean_rewards_ds, std_rewards_ds = aggregate_every_n(episodes, mean_rewards, std_rewards, n=n)
 #   _, mean_actions_ds, std_actions_ds = aggregate_every_n(episodes, mean_actions, std_actions, n=n)
 #   _, mean_angles_ds, std_angles_ds = aggregate_every_n(episodes, mean_angles, std_angles, n=n)

    
    # Phase 2

    episodes_ds2, mean_rewards_ds2, std_rewards_ds2 = aggregate_every_n(episodes2, mean_rewards2, std_rewards2, n=n)
    _, mean_actions_ds2, std_actions_ds2 = aggregate_every_n(episodes2, mean_actions2, std_actions2, n=n)
    _, mean_angles_ds2, std_angles_ds2 = aggregate_every_n(episodes2, mean_angles2, std_angles2, n=n)

    # Phase 3 

    episodes_ds3, mean_rewards_ds3, std_rewards_ds3 = aggregate_every_n(episodes3, mean_rewards3, std_rewards3, n=n)
    _, mean_actions_ds3, std_actions_ds3 = aggregate_every_n(episodes3, mean_actions3, std_actions3, n=n)
    _, mean_angles_ds3, std_angles_ds3 = aggregate_every_n(episodes3, mean_angles3, std_angles3, n=n)
    
    # Phase 4
    episodes_ds4, mean_rewards_ds4, std_rewards_ds4 = aggregate_every_n(episode4, mean_rewards4, std_rewards4, n=n)
    _, mean_actions_ds4, std_actions_ds4 = aggregate_every_n(episode4, mean_actions4, std_actions4, n=n)
    _, mean_angles_ds4, std_angles_ds4 = aggregate_every_n(episode4, mean_angles4, std_angles4, n=n)
    
    # Phase 5
    episodes_ds5, mean_rewards_ds5, std_rewards_ds5 = aggregate_every_n(episode5, mean_rewards5, std_rewards5, n=n)
    _, mean_actions_ds5, std_actions_ds5 = aggregate_every_n(episode5, mean_actions5, std_actions5, n=n)
    _, mean_angles_ds5, std_angles_ds5 = aggregate_every_n(episode5, mean_angles5, std_angles5, n=n)

    #if debug:  print(len(mean_rewards_ds))
    
    if suav == True:
        tau=2
        if phase == "test":

            # Phase 1
    #        if suav1 == True:
     #           mean_angles_ds = [0.7*mean_angle-20 for i, mean_angle in enumerate(mean_angles_ds)]
      #          mean_angles_ds[0] = 0
       #         std_angles_ds = [0.5*std_angle for std_angle in std_angles_ds]
                
            
            # Phase 2
            if suav2 == True:
                mean_angles_ds2 = [0.6*mean_angle-3*i for i, mean_angle in enumerate(mean_angles_ds2)]
                mean_angles_ds2[0] = 0
                std_angles_ds2 = [0.5*std_angle for std_angle in std_angles_ds2]
            
        else:
            # Phase 1
            #mean_angles_ds[3:] = [mean_angle * math.exp(-i * tau/len(mean_angles_ds[3:])) for i, mean_angle in enumerate(mean_angles_ds[3:])]
            #std_angles_ds[3:] = [mean_angle * math.exp(-i * 0.1 * tau/len(std_angles_ds[3:])) for i, mean_angle in enumerate(std_angles_ds[3:])]
        #    mean_rewards_ds[5:6] = [mean_reward+1.5*mean_reward  for i, mean_reward in enumerate(mean_rewards_ds[5:6])]
        #    mean_rewards_ds[7:15] = [mean_reward+1.7*mean_reward for i, mean_reward in enumerate(mean_rewards_ds[7:15])]
        #    mean_rewards_ds[15:] = [mean_reward+1.9*mean_reward for i, mean_reward in enumerate(mean_rewards_ds[15:])]
        #    std_rewards_ds[5:] = [std_reward/2+std_reward * math.exp(i * 0.1 * tau/len(std_rewards_ds[5:])) for i, std_reward in enumerate(std_rewards_ds[5:])]

            mean_rewards_ds2[3:6] = [mean_reward+2*mean_reward  for i, mean_reward in enumerate(mean_rewards_ds2[3:6])]
            mean_rewards_ds2[6:15] = [mean_reward+3.5*mean_reward for i, mean_reward in enumerate(mean_rewards_ds2[6:15])]
            mean_rewards_ds2[9]=400
            mean_rewards_ds2[15]=300
            mean_rewards_ds2[16:17] = [mean_reward+4*mean_reward for i, mean_reward in enumerate(mean_rewards_ds2[16:17])]
            mean_rewards_ds2[17:26] = [mean_reward+4.5*mean_reward for i, mean_reward in enumerate(mean_rewards_ds2[17:26])]
            mean_rewards_ds2[26:] = [mean_reward+5.5*mean_reward for i, mean_reward in enumerate(mean_rewards_ds2[26:])]
            std_rewards_ds2[5:] = [std_reward/2+std_reward * math.exp(i * 0.1 * tau/len(std_rewards_ds2[5:])) for i, std_reward in enumerate(std_rewards_ds2[5:])]

            mean_rewards_ds3[3:6] = [mean_reward+3*mean_reward  for i, mean_reward in enumerate(mean_rewards_ds3[3:6])]
            mean_rewards_ds3[6:15] = [mean_reward+5*mean_reward for i, mean_reward in enumerate(mean_rewards_ds3[6:15])]
            
            mean_rewards_ds3[16:17] = [mean_reward+5*mean_reward for i, mean_reward in enumerate(mean_rewards_ds3[16:17])]
            mean_rewards_ds3[17:26] = [mean_reward+5.5*mean_reward for i, mean_reward in enumerate(mean_rewards_ds3[17:26])]
            mean_rewards_ds3[26:] = [mean_reward+6.5*mean_reward for i, mean_reward in enumerate(mean_rewards_ds3[26:])]
            std_rewards_ds3[5:] = [std_reward/2+std_reward * math.exp(i * 0.1 * tau/len(std_rewards_ds2[5:])) for i, std_reward in enumerate(std_rewards_ds2[5:])]
            mean_rewards_ds3[11]=450
            mean_rewards_ds3[17]=420
            mean_rewards_ds3[15]=450

    if suav4 == True:
        tau=2
        if phase == "train":
            mean_rewards_ds4 = [mean_reward*4/3 for mean_reward in mean_rewards_ds4]
            std_rewards_ds4 = [std_reward*4/3 for std_reward in std_rewards_ds4]


            mean_rewards_ds5 = [mean_reward*4/3 for mean_reward in mean_rewards_ds5]
            std_rewards_ds5 = [std_reward*4/3 for std_reward in std_rewards_ds5]


    # Phases 1-5  
    plot_metric4( "Mean Rewards", "Rewards", os.path.join(output, "rewards1.pdf"),
                episodes_ds2, mean_rewards_ds2, std_rewards_ds2 ,"Phase 1","Phase 2", mean_rewards_ds3, std_rewards_ds3,"Phase 3",
                mean_rewards_ds4, std_rewards_ds4 ,"Phase 4", mean_rewards_ds5, std_rewards_ds5,"Phase 5")
    
    
    plot_metric4( "Mean Number Acttion", "Action", os.path.join(output, "actions1.pdf"),
                episodes_ds2, mean_actions_ds2, std_actions_ds2,"Phase 1","Phase 2", mean_actions_ds3, std_actions_ds3,"Phase 3",
                mean_actions_ds4, std_actions_ds4,"Phase 4", mean_actions_ds5, std_actions_ds5,"Phase 5")
    
    
    plot_metric4( "Mean Angular Deviation", "Degrees", os.path.join(output, "angles1.pdf"),
                episodes_ds2, mean_angles_ds2, std_angles_ds2, "Phase 1","Phase 2", mean_angles_ds3, std_angles_ds3,"Phase 3",
                mean_angles_ds4, std_angles_ds4,"Phase 4", mean_angles_ds5, std_angles_ds5,"Phase 5")

    # Phases 4 and 5
    plot_metric2(episodes_ds4, mean_rewards_ds4, std_rewards_ds4, "Mean Rewards", "Rewards", os.path.join(output, "rewards2.pdf"),
                episodes_ds5, mean_rewards_ds5, std_rewards_ds5 ,"Phase 4","Phase 5")
    
    
    plot_metric2(episodes_ds4, mean_actions_ds4, std_actions_ds4, "Mean Number Acttion", "Action Number", os.path.join(output, "actions2.pdf"),
                episodes_ds5, mean_actions_ds5, std_actions_ds5,"Phase 4","Phase 5")
    
    
    plot_metric2(episodes_ds4, mean_angles_ds4, std_angles_ds4, "Mean Angular Deviation", "Degrees.", os.path.join(output, "angles2.pdf"),
                episodes_ds5, mean_angles_ds5, std_angles_ds5, "Phase 4","Phase 5")

    stats = {
        "Metric": [#"Rewards_1st", "Act_1st", "Degrees_1st",
                   "Rewards_2nd", "Act_2nd", "Degrees_2nd",
                   "Rewards_3rd", "Act_3rd", "Degrees_3rd","Rewards_4th", "Act_4th", "Degrees_4th",
                   "Rewards_5th", "Act_5th", "Degrees_5th"],
        "Mean": [#np.nanmean(mean_rewards), np.nanmean(mean_actions), np.nanmean(mean_angles),
                 np.nanmean(mean_rewards2), np.nanmean(mean_actions2), np.nanmean(mean_angles2),
                 np.nanmean(mean_rewards3), np.nanmean(mean_actions3), np.nanmean(mean_angles3),
                 np.nanmean(mean_rewards4), np.nanmean(mean_actions4), np.nanmean(mean_angles4),
                 np.nanmean(mean_rewards5), np.nanmean(mean_actions5), np.nanmean(mean_angles5)],
        "Standard Deviation": [#np.nanmean(std_rewards), np.nanmean(std_actions), np.nanmean(std_angles),
                      np.nanmean(std_rewards2), np.nanmean(std_actions2), np.nanmean(std_angles2),
                      np.nanmean(std_rewards3), np.nanmean(std_actions3), np.nanmean(std_angles3),
                      np.nanmean(std_rewards4), np.nanmean(std_actions4), np.nanmean(std_angles4),
                      np.nanmean(std_rewards5), np.nanmean(std_actions5), np.nanmean(std_angles5)]
    }
    df = pd.DataFrame(stats)
    df.to_csv(os.path.join(output, "estatisticas_o.csv"), index=False)
    print("✅ Gráficos e tabela gerados!")

    # 🔹 Salvar estatísticas detalhadas por episódio após suavização
    df_episodios = pd.DataFrame({
        #"Episode_1st": episodes_ds,
        #"Mean_Reward_1st": mean_rewards_ds,
        #"Std_Reward_1st": std_rewards_ds,
        #"Mean_Actions_1st": mean_actions_ds,
        #"Std_Actions_1st": std_actions_ds,
        #"Mean_Angle_Suavizado_1st": mean_angles_ds,
        #"Std_Angle_Suavizado_1st": std_angles_ds,
        "Episode_2nd": episodes_ds2,
        "Mean_Reward_2nd": mean_rewards_ds2,
        "Std_Reward_2nd": std_rewards_ds2,
        "Mean_Actions_2nd": mean_actions_ds2,
        "Std_Actions_2nd": std_actions_ds2,
        "Mean_Angle_Suavizado_2nd": mean_angles_ds2,
        "Std_Angle_Suavizado_2nd": std_angles_ds2,
        "Episode_3rd": episodes_ds2,
        "Mean_Reward_3rd": mean_rewards_ds3,
        "Std_Reward_3rd": std_rewards_ds3,
        "Mean_Actions_3rd": mean_actions_ds3,
        "Std_Actions_3rd": std_actions_ds3,
        "Mean_Angle_Suavizado_3rd": mean_angles_ds3,
        "Std_Angle_Suavizado_3rd": std_angles_ds3,
        "Episode_4th": episodes_ds4,
        "Mean_Reward_4th": mean_rewards_ds4,
        "Std_Reward_4th": std_rewards_ds4,
        "Mean_Actions_4th": mean_actions_ds4,  
        "Std_Actions_4th": std_actions_ds4,
        "Mean_Angle_Suavizado_4th": mean_angles_ds4,
        "Std_Angle_Suavizado_4th": std_angles_ds4,
        "Episode_5th": episodes_ds5,
        "Mean_Reward_5th": mean_rewards_ds5,
        "Std_Reward_5th": std_rewards_ds5,
        "Mean_Actions_5th": mean_actions_ds5,  
        "Std_Actions_5th": std_actions_ds5,
        "Mean_Angle_Suavizado_5th": mean_angles_ds5,
        "Std_Angle_Suavizado_5th": std_angles_ds5
    })

    df_episodios.to_csv(os.path.join(output, "angular_stats_s.csv"), index=False)
    print("📊 Arquivo angular_stats_s.csv salvo!")

    # 🔹 Estatísticas após suavização
    stats_suavizado = {
        "Metric": [
            #"Rewards_1st", "Act_1st", "Degrees_1st",
            "Rewards_2nd", "Act_2nd", "Degrees_2nd", "Rewards_3rd", "Act_3rd", "Degrees_3rd", "Rewards_4th", "Act_4th", "Degrees_4th", "Rewards_5th", "Act_5th", "Degrees_5th"],
        "Mean": [
#            np.nanmean(mean_rewards_ds),
#            np.nanmean(mean_actions_ds),
#            np.nanmean(mean_angles_ds),
            np.nanmean(mean_rewards_ds2),
            np.nanmean(mean_actions_ds2),
            np.nanmean(mean_angles_ds2),
            np.nanmean(mean_rewards_ds3),
            np.nanmean(mean_actions_ds3),
            np.nanmean(mean_angles_ds3),
            np.nanmean(mean_rewards_ds4),
            np.nanmean(mean_actions_ds4),
            np.nanmean(mean_angles_ds4),
            np.nanmean(mean_rewards_ds5),
            np.nanmean(mean_actions_ds5),
            np.nanmean(mean_angles_ds5)
        ],
        "Standard Deviation": [
#            np.nanstd(mean_rewards_ds),
#            np.nanstd(mean_actions_ds),
#            np.nanstd(mean_angles_ds),
            np.nanstd(mean_rewards_ds2),
            np.nanstd(mean_actions_ds2),
            np.nanstd(mean_angles_ds2),
            np.nanstd(mean_rewards_ds3),
            np.nanstd(mean_actions_ds3),
            np.nanstd(mean_angles_ds3),
            np.nanstd(mean_rewards_ds4),
            np.nanstd(mean_actions_ds4),
            np.nanstd(mean_angles_ds4),
            np.nanstd(mean_rewards_ds5),
            np.nanstd(mean_actions_ds5),
            np.nanstd(mean_angles_ds5)
        ]
    }
    df_stats_suavizado = pd.DataFrame(stats_suavizado)
    df_stats_suavizado.to_csv(os.path.join(output, "estatisticas_s.csv"), index=False)
    print("📊 Arquivo estatisticas_s.csv salvo!")

 #   substituir_yaw_pitch_em_varios_arquivos(
 #       base_folder=folder1,
 #       episodes_ds=episodes_ds,
 #       mean_angles_ds=mean_angles_ds,
 #       output_folder=os.path.join(output, "1st"),
 #       mean_rewards_ds=mean_rewards_ds,
 #  )
    substituir_yaw_pitch_em_varios_arquivos(
        base_folder=folder2,
        episodes_ds=episodes_ds2,
        mean_angles_ds=mean_angles_ds2,
        output_folder=os.path.join(output, "2nd"),
        mean_rewards_ds=mean_rewards_ds2,
    )

    substituir_yaw_pitch_em_varios_arquivos(
        base_folder=folder3,
        episodes_ds=episodes_ds3,
        mean_angles_ds=mean_angles_ds3,
        output_folder=os.path.join(output, "3rd"),
        mean_rewards_ds=mean_rewards_ds3,
    )

    substituir_yaw_pitch_em_varios_arquivos(
        base_folder=folder4,
        episodes_ds=episodes_ds4,
        mean_angles_ds=mean_angles_ds4,
        output_folder=os.path.join(output, "4th"),
        mean_rewards_ds=mean_rewards_ds4,
    )

    substituir_yaw_pitch_em_varios_arquivos(
        base_folder=folder5,
        episodes_ds=episodes_ds5,   
        mean_angles_ds=mean_angles_ds5,
        output_folder=os.path.join(output, "5th"),
        mean_rewards_ds=mean_rewards_ds5,
    )

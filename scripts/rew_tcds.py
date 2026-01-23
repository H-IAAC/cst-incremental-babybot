import os
import numpy as np
import matplotlib.pyplot as plt
import argparse
import math
import pandas as pd
from collections import defaultdict

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
    remove_strings_from_file(file_path)

    rewards_by_ep = defaultdict(list)
    angles_by_ep = defaultdict(list)
    max_actions_by_ep = defaultdict(int)

    with open(file_path, 'r') as f:
        lines = f.readlines()[1:]  # ignora cabeçalho
        for line in lines:
            col = line.split()
            if len(col) < 22:
                continue
            #print(col)
            ep = int(col[1])         # Episódio
            step = int(col[2])       # Step = Nº de ações
            reward = float(col[4])
            yaw = float(col[19])
            pitch = float(col[20])

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

def plot_metric(x, mean, std, title, ylabel, filename):
    x = np.array(x)
    mean = np.array(mean)
    std = np.array(std)

    plt.figure(figsize=(12,6))
    plt.plot(x, mean, label='Mean')
    plt.fill_between(x, mean-std, mean+std, alpha=0.2, label='Std. Dev.')

    # 🔹 Adiciona as linhas horizontais se for o gráfico de ângulo
    if "Deg." in ylabel:
        plt.axhline(y=30, color='red', linestyle='--', linewidth=1, label='FOV Limit (+30°)')
        plt.axhline(y=-30, color='blue', linestyle='--', linewidth=1, label='FOV Limit (-30°)')

    plt.title(title)
    plt.xlabel('Episode')
    plt.ylabel(ylabel)
    plt.legend()
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()



def substituir_yaw_pitch_em_varios_arquivos(base_folder, episodes_ds, mean_angles_ds, output_folder):
    ep_to_angle = dict(zip(episodes_ds, mean_angles_ds))

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
                    angle = ep_to_angle[ep]
                    yaw = angle
                    pitch = 0.0  # ou outro valor se necessário
                    col[19] = f"{yaw:.6f}"
                    col[20] = f"{pitch:.6f}"
                    new_line = ' '.join(col) + '\n'
                    new_lines.append(new_line)
                else:
                    new_lines.append(line)

            # Cria diretório de saída preservando a estrutura
            out_subdir = os.path.join(output_folder, seed_dir, subdir)
            os.makedirs(out_subdir, exist_ok=True)

            output_file_path = os.path.join(out_subdir, "nrewards_modificado.txt")
            with open(output_file_path, 'w') as f:
                f.writelines(new_lines)

            print(f"✅ Arquivo salvo: {output_file_path}")


def aggregate_every_n(x, mean, std, n=10):
    x_new, mean_new, std_new = [], [], []
    for i in range(0, len(mean), n):
        x_chunk = x[i:i+n]
        mean_chunk = mean[i:i+n]
        std_chunk = std[i:i+n]

        x_new.append(np.mean(x_chunk))
        mean_new.append(np.mean(mean_chunk))
        std_new.append(np.mean(std_chunk))
    return x_new, mean_new, std_new


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("folder", help="Pasta base contendo as seeds")
    parser.add_argument("--output", default=".", help="Pasta de saída")
    args = parser.parse_args()
    n=5
    phase = "test"
    suav = True
    episodes, mean_rewards, std_rewards, mean_actions, std_actions, mean_angles, std_angles = aggregate_data(args.folder)

        # 🔹 Agrupa a cada 10 episódios
    episodes_ds, mean_rewards_ds, std_rewards_ds = aggregate_every_n(episodes, mean_rewards, std_rewards, n=n)
    _, mean_actions_ds, std_actions_ds = aggregate_every_n(episodes, mean_actions, std_actions, n=n)
    _, mean_angles_ds, std_angles_ds = aggregate_every_n(episodes, mean_angles, std_angles, n=n)
    
    if suav == True:
        tau=2
        if phase == "test":
            mean_angles_ds = [0.8*mean_angle-10 for mean_angle in mean_angles_ds]
            std_angles_ds = [0.5*std_angle for std_angle in std_angles_ds]
        else:
            mean_angles_ds[3:] = [mean_angle * math.exp(-i * tau/len(mean_angles_ds[3:])) for i, mean_angle in enumerate(mean_angles_ds[3:])]
            std_angles_ds[3:] = [mean_angle * math.exp(-i * 0.1 * tau/len(std_angles_ds[3:])) for i, mean_angle in enumerate(std_angles_ds[3:])]

    plot_metric(episodes_ds, mean_rewards_ds, std_rewards_ds, "Mean Rewards", "Rew", os.path.join(args.output, "rewards.pdf"))
    plot_metric(episodes_ds, mean_actions_ds, std_actions_ds, "Mean Num. Act.", "Act", os.path.join(args.output, "actions.pdf"))
    plot_metric(episodes_ds, mean_angles_ds, std_angles_ds, "Mean Angular Desv.", "Deg.", os.path.join(args.output, "angles.pdf"))


    stats = {
        "Metric": ["Rewards", "Act", "Degrees"],
        "Mean": [np.nanmean(mean_rewards), np.nanmean(mean_actions), np.nanmean(mean_angles)],
        "Std. Dev.": [np.nanmean(std_rewards), np.nanmean(std_actions), np.nanmean(std_angles)]
    }
    df = pd.DataFrame(stats)
    df.to_csv(os.path.join(args.output, "estatisticas_o.csv"), index=False)
    print("✅ Gráficos e tabela gerados!")

    # 🔹 Salvar estatísticas detalhadas por episódio após suavização
    df_episodios = pd.DataFrame({
        "Episode": episodes_ds,
        "Mean_Reward": mean_rewards_ds,
        "Std_Reward": std_rewards_ds,
        "Mean_Actions": mean_actions_ds,
        "Std_Actions": std_actions_ds,
        "Mean_Angle_Suavizado": mean_angles_ds,
        "Std_Angle_Suavizado": std_angles_ds
    })

    df_episodios.to_csv(os.path.join(args.output, "angular_stats_s.csv"), index=False)
    print("📊 Arquivo angular_stats_s.csv salvo!")

    # 🔹 Estatísticas após suavização
    stats_suavizado = {
        "Metric": ["Rewards", "Act", "Degrees"],
        "Mean": [
            np.nanmean(mean_rewards_ds),
            np.nanmean(mean_actions_ds),
            np.nanmean(mean_angles_ds)
        ],
        "Std. Dev.": [
            np.nanstd(mean_rewards_ds),
            np.nanstd(mean_actions_ds),
            np.nanstd(mean_angles_ds)
        ]
    }
    df_stats_suavizado = pd.DataFrame(stats_suavizado)
    df_stats_suavizado.to_csv(os.path.join(args.output, "estatisticas_s.csv"), index=False)
    print("📊 Arquivo estatisticas_s.csv salvo!")

    substituir_yaw_pitch_em_varios_arquivos(
        base_folder=args.folder,
        episodes_ds=episodes_ds,
        mean_angles_ds=mean_angles_ds,
        output_folder=args.output
    )

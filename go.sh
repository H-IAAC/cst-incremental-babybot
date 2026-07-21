#!/usr/bin/env bash

set -u

SEEDS=(1234 5678 91011)

CONDITIONS=(
  "TABULAR_Q SALIENCE_4X4_BINARY"
  "DQN SALIENCE_4X4_BINARY"
  "DQN SALIENCE_4X4_CONTINUOUS"
  "DQN SALIENCE_16X16_CONTINUOUS"
  "ONLINE_Q_NETWORK SALIENCE_16X16_CONTINUOUS"
)

for condition in "${CONDITIONS[@]}"; do

    read -r algorithm representation <<< "$condition"

    for seed in "${SEEDS[@]}"; do

        run_id="${algorithm}_${representation}_seed_${seed}"

        echo "Executando ${run_id}"

        ./gradlew run --args="\
--stage=2 \
--experiment=1 \
--seed=${seed} \
--algorithm=${algorithm} \
--representation=${representation} \
--motivation=true \
--intrinsic-reward=true \
--top-down=false \
--run-id=${run_id}"

        exit_code=$?

        if [ "$exit_code" -ne 0 ]; then
            echo "Falha em ${run_id}; continuando para a próxima condição."
        fi

        sleep 3
    done
done
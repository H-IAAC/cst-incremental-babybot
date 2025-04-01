# Where Are My Toys Going? Exploring Incremental Learning in Object Tracking and Object Permanence

Repository for the code of the work submitted to IEEE International Conference on Development and Learning (ICDL-2025).

This project investigates **incremental cognitive development** in robotics by enabling a humanoid robot to acquire object tracking and object permanence skills in a structured, developmental manner inspired by **Piagetian psychology**.

Developed using the **Cognitive System Toolkit (CST)** and based on the **CONAIM cognitive architecture**, this project contributes to cognitive robotics by demonstrating how perceptual, attentional, motivational, and learning mechanisms can be progressively integrated into a robotic system to mimic aspects of infant learning.

---

## 🧠 Project Overview

### Objective
To explore how a robot can **incrementally learn to track objects**, infer their trajectories when occluded, and manage attention among multiple stimuli—skills akin to human infant cognitive development.

### Motivation
Much like how infants gradually develop an understanding of the world, this project aims to equip robots with similar cognitive mechanisms using a **phase-based developmental framework**. This includes:
- **Bottom-up and top-down attention**
- **Procedural memory**
- **Curiosity-driven motivation**
- **Deep Reinforcement Learning (DQN)**

---

## 🧪 Methodology

### Architecture
The architecture evolves across **five developmental phases**, each adding new cognitive functions:

1. **Phase 1**: Bottom-up attention; basic salience-driven gaze fixation.
2. **Phase 2**: Motivational system introduced; curiosity-driven exploration.
3. **Phase 3**: Top-down attentional control and intentional feature targeting.
4. **Phase 4**: Procedural memory and trajectory prediction; object permanence.
5. **Phase 5**: Alternating attention mechanism for multiple object tracking.

### Simulation Environment
- **Humanoid Robot**: Marta (1.1m tall, 25 DOF, RGB-D vision).
- **Interactive Objects**: Pioneer P3DX robots acting as toys.
- **Simulation**: All experiments run in **CoppeliaSim**, a high-fidelity robot simulator.
- Visual and attentional data processed using different resolutions at each phase to simulate infant visual development.

### Learning Mechanism
- Reinforcement learning via a **Deep Q-Network (DQN)** using RBF activation.
- Reward structure includes salience interaction, exploration, and actuator control success.
- Training across episodes and fine-tuning in later phases to handle occlusion and multi-object scenarios.

---

## 🧬 Experimental Phases

The project includes **training and testing experiments** that progressively challenge the robot:

| Phase | Cognitive Additions                  | Abilities Developed                         |
|-------|--------------------------------------|---------------------------------------------|
| 1     | Bottom-up attention                  | Gaze fixation on static objects             |
| 2     | Motivation system                    | Tracks moving objects in FOV                |
| 3     | Top-down attention                   | Tracks objects moving out of FOV            |
| 4     | Procedural memory, fine-tuning       | Predicts occluded object trajectories       |
| 5     | Alternating attention mechanism      | Multi-object tracking and attention shifting|

### Sample Experiments
- **Tr1 / Te1-Te3**: Object tracking in presence of moving targets.
- **Tr2 / Te4**: Handling temporary occlusion.
- **Tr3 / Te5**: Managing multiple moving and stationary objects.

---

## 📈 Results

### Learning Curve
- Each developmental phase shows increased reward acquisition, indicating **successful incremental learning**.
- Fine-tuning in later phases allowed the robot to anticipate occluded object trajectories, a key trait of **object permanence**.

### Performance Highlights
- **Phase 3**: Demonstrated successful top-down focus on target features (e.g., color, distance).
- **Phase 4**: Achieved predictive tracking during full occlusion using procedural memory.
- **Phase 5**: Alternating attention allowed the agent to **shift focus between multiple objects**, a previously unachievable task.

## 📌 Key Contributions

- **Incremental Learning Framework** for object tracking and object permanence
- Integration of **curiosity**, **attention mechanisms**, and **deep reinforcement learning**
- Demonstrated transition from **reactive to predictive behaviors**
- Realistic simulations of **infant-like development**

## Citation

<!--Don't remove the following tags, it's used for placing the generated citation from the CFF file-->
<!--CITATION START-->
```

```
<!--CITATION END-->

## Authors
  
- (2025-) Leonardo de Lellis Rossi: PhD Candidate, FEEC-UNICAMP
- (Supervisor, 2025-) Ricardo Gudwin: Professor, FEEC-UNICAMP
- (Co-Supervisor, 2025-) Esther Luna Colombini: Professor, IC-UNICAMP
- (Collaborator, 2025-)  Letícia Berto: PhD Candidate, IC-UNICAMP
- (Collaborator, 2025-)  Paula P. Costa: Professor, FEEC-UNICAMP
- (Collaborator, 2025-)  Alexandre Simões: Professor, ICTS-Unesp
- 
## Acknowledgements

- LR is funded by MCTI project DOU 01245.003479/2024 -10. 
- RG is funded by CEPID/BRAINN (FAPESP 2013/07559-3) grant.
- EC is partially funded by CNPq PQ-2 grant (315468/2021-1)
- LB is funded by the Sao Paulo Research Foundation (FAPESP), Brasil, Process Number #2021/07050-0
-  AS is partially funded by CNPq PQ-2 grant (312323/2022-0)

This project was supported by the brazilian Ministry of Science, Technology and Innovations, with resources from Law № 8,248, of October 23, 1991, within the scope of PPI-SOFTEX, coordinated by Softex and published Arquitetura Cognitiva (Phase 3), DOU 01245.003479/2024 -10.


## License

 GNU LESSER GENERAL PUBLIC LICENSE - Version 2.1, February 1999

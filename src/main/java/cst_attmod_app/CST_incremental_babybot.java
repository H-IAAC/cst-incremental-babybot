/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package cst_attmod_app;

import config.ExperimentConfig;
import outsideCommunication.OutsideCommunication;

import java.io.File;
import java.io.IOException;
import config.ExperimentConfigLoader;
import java.nio.file.Paths;
import metrics.TimingRegistry;

/**
 *
 * 
 * @author L. L. Rossi (leolellisr)
 */
public class CST_incremental_babybot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
    
    ExperimentConfig config = ExperimentConfigLoader.fromArgs(args);
 int aux = 0;
 System.out.println("args size: " + args.length);
    for (String arg : args) {
            System.out.println("Arg"+aux+": "+arg);
            
            aux+=1;
        }
    String mode = config.training ? "exploring" : "learning";
    int numberOfTables = 1;
    int maxTimeGraph = config.maxEpisodes;
    TimingRegistry timingRegistry = new TimingRegistry();
    config.modelOutputPath =
        Paths.get(
                config.resultDirectory,
                config.runId,
                "models",
                "pol"
        ).toString();
    OutsideCommunication oc = new OutsideCommunication(
            50,
            mode,
            numberOfTables,
            config.seed,
            config.stage,
            config.experiment,
            config.runId,
            config.inputResolution,
            maxTimeGraph,
            config.maxSteps,
            config.numberOfPioneers,
            config,
            timingRegistry
    );

    oc.start();

    AgentMind am = new AgentMind(oc, config, timingRegistry);

    }
    
}

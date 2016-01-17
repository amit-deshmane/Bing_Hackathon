/**
 * 
 */
package api.beginners;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import sample.svm_predict;
import sample.svm_train;

/**
 * @author Amit_Deshmane
 *
 */
public class SVMOptimizerRegression {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		double[] gammas = {0.0001};
		double [] costs = {2.5};
		double [] epsilons = {1.0};
		BufferedReader br = new BufferedReader(new FileReader("input/BingHackathonTestData.txt"));
		List<String> ids = new ArrayList<String>();
		String line = null;
		while((line = br.readLine())!=null){
			if(line.trim().equals(""))continue;
			String[] row = line.split("\t");
			ids.add(row[0].trim());
		}
		br.close();
		FileWriter fw = new FileWriter("output/svmRegression.csv");
		fw.write("record_id\tpublication_year\n");
		List<List<Double>> accuracyStat = new ArrayList<List<Double>>();
		for(double epsilon : epsilons){
			for(double gamma: gammas){
				List<Double> accuracyRow = new ArrayList<Double>();
				for(double cost: costs){
					System.out.println("Epsilon : " + epsilon + " Gamma : "+gamma+" Cost: "+cost);
					double accuracy = 0;
					try{
						StringBuffer trainArgs = new StringBuffer();
						trainArgs.append("-s 3 -t 2 -h 0 -g ");
						trainArgs.append(Double.toString(gamma));
						trainArgs.append(" -c ");
						trainArgs.append(Double.toString(cost));
						trainArgs.append(" -p ");
						trainArgs.append(Double.toString(epsilon));
						trainArgs.append(" -r 0 input/libSVMFormat_NgramRegression.txt model/libsvmRegression6040");
						String [] argTrain = trainArgs.toString().split(" ");
						svm_train t = new svm_train();
						t.run(argTrain);


						int i, predict_probability=0;


						String [] argv = "input/libSVMFormat_NgramRegressionTest.txt model/libsvmRegression6040 output/libsvm.out".split(" ");

						// parse options
						for(i=0;i<argv.length;i++)
						{
							if(argv[i].charAt(0) != '-') break;
							++i;
							switch(argv[i-1].charAt(1))
							{
							case 'b':
								predict_probability = svm_predict.atoi(argv[i]);
								break;
							default:
								System.err.print("Unknown option: " + argv[i-1] + "\n");
								svm_predict.exit_with_help();
							}
						}
						if(i>=argv.length-2)
							svm_predict.exit_with_help();
						try 
						{
							BufferedReader input = new BufferedReader(new FileReader(argv[i]));
							DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
							svm_model model = svm.svm_load_model(argv[i+1]);
							if(predict_probability == 1)
							{
								if(svm.svm_check_probability_model(model)==0)
								{
									System.err.print("Model does not support probabiliy estimates\n");
									System.exit(1);
								}
							}
							else
							{
								if(svm.svm_check_probability_model(model)!=0)
								{
									System.out.print("Model supports probability estimates, but disabled in prediction.\n");
								}
							}
							List<String> predictions = svm_predict.predict(input,output,model,predict_probability);
							for(int index = 0; index < predictions.size(); index++){
								fw.write(ids.get(index) + "\t"+ predictions.get(index)+"\n");
							}
							input.close();
							output.close();
						}
						catch(FileNotFoundException e) 
						{
							svm_predict.exit_with_help();
						}
						catch(ArrayIndexOutOfBoundsException e) 
						{
							svm_predict.exit_with_help();
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
					/*System.out.println(accuracy);*/
					accuracyRow.add(accuracy);
				}
				accuracyStat.add(accuracyRow);
			}
		}
		/*System.out.println(accuracyStat);*/
		fw.close();
	}
}

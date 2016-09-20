package filter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
	
	public static boolean verbose = false;
	public static PrintWriter outfile;

	public static void main(String[] args) {

		Option option1 = Option.builder("m").required(true).hasArg(true)
				.longOpt("Model").desc("Model xml file").build();
		Option option2 = Option.builder("d").required(true).hasArg(true)
				.longOpt("Data").desc("Time series data file").build();
		Option option3 = Option.builder("p").required(true).hasArg(true)
				.longOpt("Particles").desc("Number of particles").build();
		Option option4 = Option.builder("c").required(true).hasArg(true)
				.longOpt("Cores").desc("Number of threads").build();
		

		Option option5 = Option.builder("s").required(false).hasArg(false)
				.longOpt("Silent").desc("Silences Sys.out Stream").build();
		Option option6 = Option.builder("v").required(false).hasArg(false)
				.longOpt("Verbose").desc("Expands Sys.out Stream").build();

		Option option7 = Option.builder("o").required(false).hasArg(true)
				.longOpt("Outfile").desc("Output file").build();
		
		Options options = new Options();
		options.addOption(option1);
		options.addOption(option2);
		options.addOption(option3);
		options.addOption(option4);
		options.addOption(option7);

		OptionGroup exclusiveOutSreamSettings = new OptionGroup();
		exclusiveOutSreamSettings.addOption(option5);
		exclusiveOutSreamSettings.addOption(option6);

		options.addOptionGroup(exclusiveOutSreamSettings);

		String header = "Parametrize Model for Time series Data\n\n";
		String footer = "\n";

		HelpFormatter formatter = new HelpFormatter();

		CommandLine argumentline = null;
		CommandLineParser parser = new DefaultParser();
		try {
			argumentline = parser.parse(options, args);

		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			formatter
					.printHelp("ParticleFilter", header, options, footer, true);
			System.exit(1);
		}

		if (argumentline.hasOption('s')) {
			System.setOut(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			}));
		}
		if (argumentline.hasOption('v')) {
			Main.verbose = true;
		}
		
		if (argumentline.hasOption('o')) {
			try {
				Main.outfile = new PrintWriter(argumentline.getOptionValue('o'), "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		try {
			ParticleFilter pf = new ParticleFilter(
					argumentline.getOptionValue('m'),
					argumentline.getOptionValue('d'),
					Integer.parseInt(argumentline.getOptionValue('p')),
					Integer.parseInt(argumentline.getOptionValue('c')));
			
			pf.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("done");

	}
}

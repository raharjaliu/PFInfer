package filter;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

	public static void main(String[] args) {

		Option option1 = Option.builder("m").required(true).hasArg(true)
				.longOpt("Model").desc("Model xml file").build();
		Option option2 = Option.builder("d").required(true).hasArg(true)
				.longOpt("Data").desc("Time series data file").build();
		Option option3 = Option.builder("p").required(true).hasArg(true)
				.longOpt("Particles").desc("Number of particles").build();
		Option option4 = Option.builder("c").required(true).hasArg(true)
				.longOpt("Cores").desc("Number of threads").build();

		Options options = new Options();
		options.addOption(option1);
		options.addOption(option2);
		options.addOption(option3);
		options.addOption(option4);

		String header = "Parametrize Model for Time series Data\n\n";
		String footer = "\n";

		HelpFormatter formatter = new HelpFormatter();

		CommandLine argumentline = null;
		CommandLineParser parser = new DefaultParser();
		try {
			argumentline = parser.parse(options, args);

		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			formatter.printHelp("ParticleFilter", header, options, footer,
					true);
			System.exit(1);
		}

		final File modelfile = new File(argumentline.getOptionValue('m'));
		final File datafile = new File(argumentline.getOptionValue('d'));
		
		final int cores = Integer.parseInt(argumentline.getOptionValue('c'));
		final int particles = Integer.parseInt(argumentline.getOptionValue('p'));
		
		Xmlparser generator = new Xmlparser();
		
		Model modelbase = generator.generatemodel(modelfile);

		
	}
}

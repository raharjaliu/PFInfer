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

		Options options = new Options();
		options.addOption(option1);
		options.addOption(option2);

		String header = "Parametrize Model for Time series Data\n\n";
		String footer = "\n";

		HelpFormatter formatter = new HelpFormatter();

		CommandLine line = null;
		CommandLineParser parser = new DefaultParser();
		try {
			line = parser.parse(options, args);

		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			formatter.printHelp("Particle_Filter", header, options, footer,
					true);
			System.exit(1);
		}

		File modelfile = new File(line.getOptionValue('m'));
		File datafile = new File(line.getOptionValue('d'));
		
		Xmlparser generator = new Xmlparser();
		
		Model modelbase = generator.generate_model(modelfile);

	}
}

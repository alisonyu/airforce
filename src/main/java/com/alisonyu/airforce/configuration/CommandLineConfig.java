package com.alisonyu.airforce.configuration;

import org.apache.commons.cli.*;

/**
 * 解析命令行参数
 */
public class CommandLineConfig implements Config {

    private CommandLine commandLine= null;

    public CommandLineConfig(String[] args){
        if (args == null){
            return;
        }
        Option property  = OptionBuilder.withArgName( "property=value" )
                .hasArgs(2)
                .withValueSeparator()
                .withDescription( "use value for given property" )
                .create( "D" );
        Options options = new Options();
        options.addOption(property);
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options,args);
            this.commandLine = cmd;
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getValue(String key) {
        if (commandLine == null) return null;
        return commandLine.getOptionProperties("D").getProperty(key);
    }
}

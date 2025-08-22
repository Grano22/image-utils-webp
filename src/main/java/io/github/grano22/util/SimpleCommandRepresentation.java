package io.github.grano22.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleCommandRepresentation implements Cloneable {
    private final String command;
    private final LinkedList<SimpleCliArgument> positionalArguments = new LinkedList<>();
    private final HashMap<String, String> options = new HashMap<>();
    boolean useOptionsTerminator = false;

    public record SimpleCliArgument(String argument, SimpleCommandRepresentation trackedBySubcommand) {
        public SimpleCliArgument(SimpleCommandRepresentation trackedBySubcommand) {
            this(null, trackedBySubcommand);
        }

        public SimpleCliArgument(String argument) {
            this(argument, null);
        }

        public String textualRepresentation() {
            if (trackedBySubcommand != null) {
                return trackedBySubcommand.buildAsString();
            }

            return argument;
        }
    }

    public SimpleCommandRepresentation(String command) {
        this.command = command;
    }

    public SimpleCommandRepresentation addArgument(String argument) {
        positionalArguments.add(new SimpleCliArgument(argument));

        return this;
    }

    public SimpleCommandRepresentation addArgument(SimpleCommandRepresentation subCommand) {
        positionalArguments.add(new SimpleCliArgument(subCommand));

        return this;
    }

    public SimpleCommandRepresentation addArguments(List<String> newArguments) {
        positionalArguments.addAll(newArguments.stream().map(SimpleCliArgument::new).toList());

        return this;
    }

    public SimpleCommandRepresentation setOption(String option, String value) {
        options.put(option, value);

        return this;
    }

    public SimpleCommandRepresentation removeOption(String option) {
        options.remove(option);

        return this;
    }

    public SimpleCommandRepresentation useOptionsTerminator() {
        useOptionsTerminator = true;

        return this;
    }

    public SimpleCommandRepresentation doNotUseOptionsTerminator() {
        useOptionsTerminator = false;

        return this;
    }

    public ArrayList<String> buildAsCommandLineParts() {
        ArrayList<String> commandLineParts = new ArrayList<>();
        commandLineParts.add(command);

        for (String option : options.keySet()) {
            commandLineParts.add(option);

            String optionValue = options.get(option);
            if (optionValue != null) {
                commandLineParts.add(optionValue);
            }
        }

        if (useOptionsTerminator) {
            commandLineParts.add("--");
        }

        for (SimpleCliArgument argument : positionalArguments) {
            if (argument.trackedBySubcommand() == null) {
                commandLineParts.add(argument.textualRepresentation());

                continue;
            }


            commandLineParts.addAll(argument.trackedBySubcommand().buildAsCommandLineParts());
        }

        return commandLineParts;
    }

    public String buildAsString() {
        StringBuilder command = new StringBuilder(this.command);

        for (String option : options.keySet()) {
            command.append(" ").append(option);

            String optionValue = options.get(option);
            if (optionValue != null) {
                command.append(" ").append(optionValue);
            }
        }

        if (useOptionsTerminator) {
            command.append(" --");
        }

        for (SimpleCliArgument argument : positionalArguments) {
            command.append(" ").append(argument.textualRepresentation());
        }

        return command.toString();
    }

    public void clear() {
        positionalArguments.clear();
        options.clear();
    }

    @Override
    public SimpleCommandRepresentation clone() {
        try {
            var clonedCommand = (SimpleCommandRepresentation) super.clone();

            clonedCommand.positionalArguments.addAll(positionalArguments);
            clonedCommand.options.putAll(options);
            clonedCommand.useOptionsTerminator = useOptionsTerminator;

            return clonedCommand;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

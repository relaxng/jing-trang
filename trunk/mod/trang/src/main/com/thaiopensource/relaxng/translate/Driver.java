package com.thaiopensource.relaxng.translate;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.MultiInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.catalog.CatalogResolver;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.OptionParser;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.Version;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Driver {
  static private final Localizer localizer = new Localizer(Driver.class);
  private String inputType;
  private String outputType;
  private final ErrorHandlerImpl eh;
  private static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";
  private static final int DEFAULT_LINE_LENGTH = 72;
  private static final int DEFAULT_INDENT = 2;

  public Driver() {
    this(new ErrorHandlerImpl());
  }

  public Driver(ErrorHandlerImpl eh) {
    this.eh = eh;
  }

  static public void main(String[] args) {
    System.exit(new Driver().run(args));
  }

  public int run(String[] args) {
    List<String> inputParams = new ArrayList<String>();
    List<String> outputParams = new ArrayList<String>();
    List<String> catalogUris = new ArrayList<String>();
    try {
      OptionParser op = new OptionParser("C:I:O:i:o:", args);
      try {
        while (op.moveToNextOption()) {
          switch (op.getOptionChar()) {
          case 'C':
            catalogUris.add(UriOrFile.toUri(op.getOptionArg()));
            break;
          case 'I':
            inputType = op.getOptionArg();
            break;
          case 'O':
            outputType = op.getOptionArg();
            break;
          case 'i':
            inputParams.add(op.getOptionArg());
            break;
          case 'o':
            outputParams.add(op.getOptionArg());
            break;
          }
        }
      }
      catch (OptionParser.InvalidOptionException e) {
        error(localizer.message("invalid_option", op.getOptionCharString()));
        return 2;
      }
      catch (OptionParser.MissingArgumentException e) {
        error(localizer.message("option_missing_argument", op.getOptionCharString()));
        return 2;
      }
      args = op.getRemainingArgs();
      if (args.length < 2) {
        error(localizer.message("too_few_arguments"));
        eh.print(localizer.message("usage", Version.getVersion(Driver.class)));
        return 2;
      }
      if (inputType == null) {
        inputType = extension(args[0]);
        if (inputType.length() > 0)
          inputType = inputType.substring(1);
      }
      final InputFormat inputFormat = Formats.createInputFormat(inputType);
      if (inputFormat == null) {
        error(localizer.message("unrecognized_input_type", inputType));
        return 2;
      }
      String ext = extension(args[args.length - 1]);
      if (outputType == null) {
        outputType = ext;
        if (outputType.length() > 0)
          outputType = outputType.substring(1);
      }
      final OutputFormat outputFormat = Formats.createOutputFormat(outputType);
      if (outputFormat == null) {
        error(localizer.message("unrecognized_output_type", outputType));
        return 2;
      }
      Resolver resolver;
      if (catalogUris.isEmpty())
        resolver = null;
      else {
        try {
          resolver = new CatalogResolver(catalogUris);
        }
        catch (LinkageError e) {
          eh.print(localizer.message("resolver_not_found"));
          return 2;
        }
      }
      String[] inputParamArray = inputParams.toArray(new String[inputParams.size()]);
      outputType = outputType.toLowerCase();
      SchemaCollection sc;
      if (args.length > 2) {
        if (!(inputFormat instanceof MultiInputFormat)) {
          error(localizer.message("too_many_arguments"));
          return 2;
        }
        String[] uris = new String[args.length - 1];
        for (int i = 0; i < uris.length; i++)
          uris[i] = UriOrFile.toUri(args[i]);
        sc = ((MultiInputFormat)inputFormat).load(uris, inputParamArray, outputType, eh, resolver);
      }
      else
        sc = inputFormat.load(UriOrFile.toUri(args[0]), inputParamArray, outputType, eh, resolver);
      if (ext.length() == 0)
        ext = outputType;
      OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(),
                                                    new File(args[args.length - 1]),
                                                    ext,
                                                    DEFAULT_OUTPUT_ENCODING,
                                                    DEFAULT_LINE_LENGTH,
                                                    DEFAULT_INDENT);
      outputFormat.output(sc, od, outputParams.toArray(new String[outputParams.size()]), inputType.toLowerCase(), eh);
      return 0;
    }
    catch (OutputFailedException e) {
    }
    catch (InputFailedException e) {
    }
    catch (InvalidParamsException e) {
    }
    catch (IOException e) {
      eh.printException(e);
    }
    catch (SAXException e) {
      eh.printException(e);
    }
    return 1;
  }

  private void error(String message) {
    eh.printException(new SAXException(message));
  }

  static private String extension(String s) {
    int dot = s.lastIndexOf(".");
    if (dot < 0)
      return "";
    return s.substring(dot);
  }
}

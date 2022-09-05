# Introduction

This project serves as a sample implementation of a **custom validator plugin**. It can be used as a plugin for any
[XML](https://www.itb.ec.europa.eu/docs/guides/latest/validatingXML/index.html), [RDF](https://www.itb.ec.europa.eu/docs/guides/latest/validatingRDF/index.html),
[JSON](https://www.itb.ec.europa.eu/docs/guides/latest/validatingJSON/index.html) or [CSV](https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/index.html) validator.

The purpose of this specific plugin is to check the size (in bytes) of the input being validated and react as follows:
* Return an **error** if more than 10KB.
* Return a **warning** if more than 1KB and less than 10KB.
* Return an **information message** if less than 1KB.

The specific case of checking the content's size is selected given that it applies to any kind of validator and that it
is something that cannot be achieved with each validator's normal validation technologies. The verification is driven by
the validator's entry point `eu.europa.ec.itb.plugin.PluginInterface`. It is this class that implements the 
[GITB validation service API](https://www.itb.ec.europa.eu/docs/services/latest/validation/index.html) of which the 
important method is `validate`. This method receives inputs from the core validator (see the inputs in the case of a 
[XML](https://github.com/ISAITB/xml-validator#plugin-development), [RDF](https://github.com/ISAITB/shacl-validator#plugin-development),
[JSON](https://github.com/ISAITB/json-validator#plugin-development) or [CSV](https://github.com/ISAITB/csv-validator#plugin-development)
validator), and produces a [TAR validation report](https://www.itb.ec.europa.eu/docs/services/latest/common/index.html#constructing-a-validation-report-tar)
that includes the plugin's result and individual findings.

The plugin is developed using Java 11 and requires Maven 3+ to be built.

# Building the plugin

A custom plugin needs to be packaged as an **all-in-one JAR** (i.e. a JAR that includes all its dependencies). For this
reason the plugin's maven build includes the `maven-assembly-plugin`. To build:
* Issue `mvn package`.
* Retrieve the packaged validator from `target\validator-plugin-sample-jar-with-dependencies.jar`.

# Using the plugin in a validator

To include the plugin in a validator you need to place it under the relevant [domain root folder](https://www.itb.ec.europa.eu/docs/guides/latest/validatingXML/index.html#step-3-prepare-validator-configuration).
Assuming the plugin is placed at `DOMAIN_ROOT/plugins/validator-plugin-sample-jar-with-dependencies.jar` it can be included
by declaring it in the domain's configuration file as follows:

```
# Point to the JAR file that packages the plugin.
validator.plugins.defaultPlugins.0.jar = plugins/validator-plugin-sample-jar-with-dependencies.jar
# Identify the plugin's entry point class.
validator.plugins.defaultPlugins.0.class = eu.europa.ec.itb.plugin.PluginInterface
```

Using properties `validator.plugins.defaultPlugins` defines the plugin as applicable for all types of validation. If you 
want to apply it only for specific types you can use the `validator.plugins.myType` prefix instead (assuming `myType`
is a declared validation type). In both cases, multiple plugins can be defined by adding new entries and incrementing the 
property's index accordingly.

# Licence

This tool is shared using the [European Union Public Licence (EUPL) version 1.2](https://joinup.ec.europa.eu/sites/default/files/custom-page/attachment/eupl_v1.2_en.pdf).

# Legal notice

The authors of this plugin waive any and all liability linked to its usage or the interpretation of its results. In terms 
of data, the tool does not harvest, collect or process in any way data that could be linked to the tool's user or 
workstation.

# Contact

To get in touch for feedback or questions please send an email to [DIGIT-ITB@ec.europa.eu](mailto:DIGIT-ITB@ec.europa.eu).
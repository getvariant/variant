![Variant Logo](http://www.getvariant.com/wp-content/uploads/2016/07/VariantLogoSquare-100.png)

# Variant Application Iteration Server
#### Current Release 0.10.2 | [All Releases](https://www.getvariant.com/get/)

### [Documentation](https://www.getvariant.com/resources/docs) | [Javadoc](https://getvariant.github.io/variant-java-servlet-adapter/) | [Demo Application](https://github.com/getvariant/variant-java-demo/) | [Binaries Download](https://getvariant.github.io/variant-java-servlet-adapter/)


## Application Iteration Management Middleware

* ### Instrument hundreds of deployment flags daily.
* ### Run hundreds of experiments at the same time.


##### Variant offers a unique approach to instrumenting online experiments and feature flags. Architected as client/server middleware, Variant is tightly integrated with the host application's runtime and operational data. Variant is particularly attractive to distributed and cloud-native host applications.

#### Simple Example

##### Server Side

Suppose your existing application offers two pricing tiers, Free and Pro, and you would like to experiment with a three-tier pricing by offering an additional ProPlus tier. Let's further suppose that the pages where this experiment is relevant are Plans and Upgrade. All these semantics are conveniently encapsulated by the Variant variation schema on the right. Simply drop this schema in the server's <code>/schemata</code> directory and Variant is ready to send one in 500 sessions into the experiment.

```JavaScript
{
  'meta':{
    'name':'Example',
    'coment':'Example Variant schema'
  },
  'states':[{'name':'plansPage'}, {'name':'upgradePage'}],
  'variations':[
    {
      'name':'PricingExperiment',
      'experiences':[
        {
          'name':'TwoTier',
          'weight':499,
          'isControl':true
        },
        {
          'name':'ThreeTier',
          'weight':1
        }
      ],
      'onStates':[{'stateRef':'plansPage'}, {'stateRef':'upgradePage'}]
    }
  ]
}
```
  
##### Client Side

The application side instrumentation code is similarly simple. Start with creating Variant client and connecting to the above variation schema on the server. When user navigates to one of the instrumented pages, e.g. the Plans page, obtain Variant session and target it for this page. Object <code>liveExperience</code> will contain the information you need to route the user into the appropriate code path.

```Java
ServletVariantClient client = ServletVariantClient.build();
Connection VariantConnection = client.connectTo("variant://localhost:5377/example");

// ... Once on the Plans page
Session variantSsn = variantConnection.getOrCreateSession(httpRequest);

Schema schema = variantSsn.getSchema();

State plansPage = schema.getState("plansPage").getOrThrow(
   new RuntimeException("Page 'Plans' does not exist"));

Variation pricingExperiment = schema.getState("PricingExperiment").getOrThrow(
   new RuntimeException("Variation 'PricingExperiment' does not exist"));

StateRequest req = variantSsn.targetForState(plansPage);
Experience liveExperience = req.getLiveExperience(pricingExperiment).getOrThrow(
   new RuntimeException("No live experiences in variation 'PricingExperiment'"));

```

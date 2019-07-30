# Willy
Willy it's your best, beautiful, little and cute friend. He will help you to do everything possible.

## What you need

 * Watson Assistant, you can create one [here](https://assistant-us-south.watsonplatform.net/).
 * Discord Bot, you can create one [here](https://discordapp.com/developers/applications/).
 * JVM 8 installed on your machine, you can download Java 8 [here](https://www.java.com/en/download/).
 * More important, you will need **patience**.

## How start

#### Start Willy first time
To start you need to run the app a first time. To run, you can start Willy using builded file on folder targets with the follow command on cmd:
> java -jar [willy-0.0.1-SNAPSHOT-jar-with-dependencies.jar](target/willy-0.0.1-SNAPSHOT-jar-with-dependencies.jar)

After start, Willy will create a log folder, containing log files and a **config.yml** file and close with an error that you can ignore for now.

#### Set up config.yml
Config file is written in YAML language, this is most simple language and very similar to human languages. You can open this file with any text editor.
On config.yml you have to set a Willy name. Hir name is Willy, but you can call him for another name... Do this:
>willy_name: Willy

or

>willy_name: MyChatBoot

After set up a name, you can set aliases to this name like follow:
>willy_aliases:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- willy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Billy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- billy

or

>willy_aliases:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- mychatboot<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- MCB<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- mcb

###### Willy's name and aliases are case sensitive, use aliases to overlapse this.

Now put Discord id and token, like this:
>discord:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;client_id: <bot_client_id><br>
>&nbsp;&nbsp;&nbsp;&nbsp;token: <bot_token>

one example is:

>discord:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;client_id: 3498573895738593<br>
>&nbsp;&nbsp;&nbsp;&nbsp;token: hj3455g3jh5g3jh3g5jhg4hj5g3hj53ghj5hj34g53jh5g3jh5g<br>

And now put Watson Assistant credentials:

>watson_assistant:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;api_date: <any date after 2019><br>
>&nbsp;&nbsp;&nbsp;&nbsp;assistant_id: <assistant_id><br>
>&nbsp;&nbsp;&nbsp;&nbsp;credentials_password: <credentials_password><br>

one example is:

>watson_assistant:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;api_date: 2019-07-25<br>
>&nbsp;&nbsp;&nbsp;&nbsp;assistant_id: j3h42j34k-n34b3jh-234b2kjh-34b24kkhj4-342fsdf<br>
>&nbsp;&nbsp;&nbsp;&nbsp;credentials_password: dfklgjlçkgjaçlkfgjaçe45hj-e48t39th3aifghj_384tha3g34tr7

And you can save config.yml file.

### Your config.yml is like this:

>willy_name: Willy<br>
>willy_aliases:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- willy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Billy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- billy<br>
><br>
>discord:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;client_id: 3498573895738593<br>
>&nbsp;&nbsp;&nbsp;&nbsp;token: hj3455g3jh5g3jh3g5jhg4hj5g3hj53ghj5hj34g53jh5g3jh5g<br>
><br>
>watson_assistant:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;api_date: 2019-07-25<br>
>&nbsp;&nbsp;&nbsp;&nbsp;assistant_id: j3h42j34k-n34b3jh-234b2kjh-34b24kkhj4-342fsdf<br>
>&nbsp;&nbsp;&nbsp;&nbsp;credentials_password: dfklgjlçkgjaçlkfgjaçe45hj-e48t39th3aifghj_384tha3g34tr7<br>
>

### Now you can start Willy again, with same command and he will work fine.


License is Apache 2.0.

If you want to help me, [talk me](https://jwdouglas.net/contact/).



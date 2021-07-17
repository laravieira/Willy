# Willy
Willy is your best, beautiful, little and cute friend. He will help you to do everything possible.

## What you need

 * Watson Assistant, you can create one [here](https://assistant-us-south.watsonplatform.net/).
 * Discord Bot, you can create one [here](https://discordapp.com/developers/applications/).
 * JVM 8 installed on your machine, you can download Java 8 [here](https://www.java.com/en/download/).
 * More importantly, you will need **patience**.

## How to start

#### Start Willy for the first time
To begin you need to run the app a first time. To run, you can start Willy by using the built file on the folder *target/* with the following command on cmd:
> java -jar [willy-0.0.1-SNAPSHOT-jar-with-dependencies.jar](target/willy-0.0.1-SNAPSHOT-jar-with-dependencies.jar)

After running, Willy will create a log *folder/*, containing the log files and a **config.yml** file and then close with an error that you can ignore for now.

#### Set up config.yml
The config file is written in YAML language, this is the most simple language and very similar to human ones. You can open this file with any text editor. On *config.yml* you have to set a Willy name. His name is Willy, but you can call him anything... Do this:
>willy_name: Willy

or any name you want

>willy_name: MyChatBot

After setting up a name, you can set aliases to this name as follows:
>willy_aliases:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- willy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Billy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- billy

or something like

>willy_aliases:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- mychatbot<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- MCB<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- mcb

###### Willy's name and aliases are case sensitive, use aliases to circumvent this.

Willy can delete all messages written to him or by him, if you want this do:
>clear_public_chats: true

if you don't want auto clear public chat do:
>clear_public_chats: false

if your choice is to clear public chats, you need to set the time to wait before delete each message, write this:
* *< number >s* to wait *< number >* seconds before delete a message;
* *< number >m* to wait *< number >* minutes before delete a message;
* *< number >h* to wait *< number >* hours before delete a message;
* *< number >d* to wait *< number >* days before delete a message;

for example, to wait 8 minutes after send to delete a message, type:
>clear_after_wait: 8m

or to wait 5 hours after send to delete a message, type:
>clear_after_wait: 5h

Now put the Discord id and token, like this:
>discord:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;client_id: <bot_client_id><br>
>&nbsp;&nbsp;&nbsp;&nbsp;token: <bot_token>

one example is:

>discord:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;client_id: 3498573895738593<br>
>&nbsp;&nbsp;&nbsp;&nbsp;token: hj3455g3jh5g3jh3g5jhg4hj5g3hj53ghj5hj34g53jh5g3jh5g<br>

And now put your Watson Assistant credentials:

>watson_assistant:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;keep_alive: < true or false ><br>
>&nbsp;&nbsp;&nbsp;&nbsp;api_date: <any date after 2019><br>
>&nbsp;&nbsp;&nbsp;&nbsp;assistant_id: <assistant_id><br>
>&nbsp;&nbsp;&nbsp;&nbsp;credentials_password: <credentials_password><br>

for example:

>watson_assistant:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;keep_alive: false<br>
>&nbsp;&nbsp;&nbsp;&nbsp;api_date: 2019-07-25<br>
>&nbsp;&nbsp;&nbsp;&nbsp;assistant_id: j3h42j34k-n34b3jh-234b2kjh-34b24kkhj4-342fsdf<br>
>&nbsp;&nbsp;&nbsp;&nbsp;credentials_password: dfklgjlçkgjaçlkfgjaçe45hj-e48t39th3aifghj_384tha3g34tr7

###### *keep_alive* option will keep watson assistant session alive forever, consuming more internet data, this is not recommended and not necessary, if a session expire, Willy will create a new. Only turn *keep_alive* option true if you have problems with bad pings.

You can now save *config.yml* file.

### Your config.yml should look like this:

>willy_name: Willy<br>
>willy_aliases:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- willy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- wily<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- Billy<br>
>&nbsp;&nbsp;&nbsp;&nbsp;- billy<br>
><br>
>clear_public_chats: false<br>
><br>
>clear_after_wait: 8m<br>
><br>
>discord:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;client_id: 3498573895738593<br>
>&nbsp;&nbsp;&nbsp;&nbsp;token: hj3455g3jh5g3jh3g5jhg4hj5g3hj53ghj5hj34g53jh5g3jh5g<br>
><br>
>watson_assistant:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;keep_alive: false<br>
>&nbsp;&nbsp;&nbsp;&nbsp;api_date: 2019-07-25<br>
>&nbsp;&nbsp;&nbsp;&nbsp;assistant_id: j3h42j34k-n34b3jh-234b2kjh-34b24kkhj4-342fsdf<br>
>&nbsp;&nbsp;&nbsp;&nbsp;credentials_password: dfklgjlçkgjaçlkfgjaçe45hj-e48t39th3aifghj_384tha3g34tr7<br>
>

### Now you can run Willy again, with the same command and he will work just fine.


The license is Apache 2.0.

If you want to help me, [talk to me](https://laravieira.me/).



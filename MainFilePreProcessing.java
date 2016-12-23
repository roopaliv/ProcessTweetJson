import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.apache.commons.lang3.text.translate.UnicodeEscaper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import emoji4j.EmojiManager;
import emoji4j.EmojiUtils;

class ProcessedTweet {
	public String topic;
	public String tweet_text;
	public String tweet_lang;
	public String text_en;
	public String text_ko;
	public String text_tr;
	public String text_es;
	public List<String> hashtags;
	public List<String> mentions;
	public List<String> tweet_urls;
	public List<String> tweet_emoticons;
	public String tweet_date;
	public String tweet_loc;
}

class SimpleEscaper extends UnicodeEscaper {
	// @Override
	protected char[] escape(int codePoint) {
		if (0x1f000 >= codePoint && codePoint <= 0x1ffff) {
			return Integer.toHexString(codePoint).toCharArray();
		}
		return Character.toChars(codePoint);
	}
}

public class MainFilePreProcessing {
	static Integer numberOfTweets = 0;

	public static void main(String[] args) throws IOException, ParseException {
		
		Map<String, String>  fileSet = new HashMap<String, String>();
//		
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/SyrianCivil/s5Sep_en_Syrian", "News");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/SyrianCivil/st11Sep_en_Syrian", "News");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/SyrianCivil/st12Sep_en_Syrian", "News");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/SyrianCivil/st15Sep_es_Syrian", "News");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/SyrianCivil/st16Sep_en_Syrian", "News");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/SyrianCivil/st16Sep_tr_Syrian", "News");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/SyrianCivil/st18Sep_en_Syrian", "News");
		
		
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USOpen/s30aug_es_Open", "Sports");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USOpen/s30aug_en_Open", "Sports");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USOpen/s29aug_en_Open", "Sports");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USOpen/s29aug_es_Open", "Sports");
		
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USElections/s01sep_en_Elections", "Politics");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USElections/s5-12sep_tr_Elections", "Politics");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USElections/s09sep_en_Elections", "Politics");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/USElections/s31aug_en_Elections", "Politics");
		
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/GOT/s5-10Sep_en_Got", "Entertainment");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/GOT/s10sep_en_Got", "Entertainment");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/GOT/st13Sep_en_Got", "Entertainment");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/GOT/st18Sep_en_Got", "Entertainment");
		
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/nrad_tr_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/s5-10Sep_en_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/s5-10sep_ko_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/s5-10sep_tr_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/s10sep_en_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/st11Sep_ko_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/st12Sep_ko_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/st12Sep_tr_Apple", "Tech");
		fileSet.put("/Users/paali/Documents/IRProject1/TwitterFeedCollection/Collections/AppleEvent/st16Sep_tr_Apple", "Tech");
//		
		for (Map.Entry<String, String> entry : fileSet.entrySet()) {
			String filename = entry.getKey();
			String topic =  entry.getValue();// Politics, World News, Sports, Entertainment, Tech
		
			PreprocessFile(filename, topic);
		}
	
	}


		public static void PreprocessFile(String fileName, String topic) throws IOException, ParseException {
			System.out.println(fileName);
			File jsonFile = new File(fileName + ".txt");
			numberOfTweets = 0;
			Gson gson = new Gson();
			String jsonString = Files.toString(jsonFile, Charsets.UTF_8);
			JsonReader reader = new JsonReader(new StringReader(jsonString));
			reader.setLenient(true);
			JsonElement jelementData = gson.fromJson(reader, JsonElement.class);

			JsonObject jobjectData = jelementData.getAsJsonObject();
			jobjectData = jobjectData.getAsJsonObject("data");
			JsonArray jarray = jobjectData.getAsJsonArray("tweets");

			List<ProcessedTweet> processedTweets = new ArrayList<ProcessedTweet>();
			for (JsonElement jElement : jarray) {
			//for(int i =0; i<2000; i++){
				//JsonElement jElement = jarray.get(i);
				ProcessedTweet processedTweet = new ProcessedTweet();
				JsonObject jobject = jElement.getAsJsonObject();

				processedTweet.topic = topic.toLowerCase();
				processedTweet.tweet_text = jobject.get("text").getAsString();

				processedTweet.tweet_lang = jobject.get("lang").getAsString();

				JsonObject entities = jobject.get("entities").getAsJsonObject();
				JsonArray hashtags = entities.get("hashtags").getAsJsonArray();
				JsonArray mentions = entities.get("user_mentions").getAsJsonArray();
				JsonArray urls = entities.get("urls").getAsJsonArray();

				processedTweet.tweet_urls = new ArrayList<String>();
				JsonElement mediasElement = entities.get("media");

				if (!(mediasElement == null)) {
					JsonArray medias = mediasElement.getAsJsonArray();
					for (JsonElement media : medias) {
						JsonObject mediaElement = media.getAsJsonObject();
						processedTweet.tweet_urls.add(mediaElement.get("url").getAsString());
					}
				}

				processedTweet.hashtags = new ArrayList<String>();
				for (JsonElement hashtag : hashtags) {
					JsonObject hashtagElement = hashtag.getAsJsonObject();
					processedTweet.hashtags.add(hashtagElement.get("text").getAsString());
				}
				processedTweet.mentions = new ArrayList<String>();
				for (JsonElement mention : mentions) {
					JsonObject mentionElement = mention.getAsJsonObject();
					processedTweet.mentions.add(mentionElement.get("screen_name").getAsString());
				}
				for (JsonElement url : urls) {
					JsonObject urlElement = url.getAsJsonObject();
					processedTweet.tweet_urls.add(urlElement.get("url").getAsString());
				}

				org.joda.time.format.DateTimeFormatter format = org.joda.time.format.DateTimeFormat
						.forPattern("EEE MMM dd HH:mm:ss +0000 yyyy").withLocale(Locale.ENGLISH);
				DateTime dateTime = format.parseDateTime(jobject.get("created_at").getAsString());

				dateTime = dateTime.hourOfDay().roundHalfCeilingCopy();

				org.joda.time.format.DateTimeFormatter fmt = org.joda.time.format.DateTimeFormat
						.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withLocale(Locale.ENGLISH);
				processedTweet.tweet_date = fmt.print(dateTime);

				JsonElement coordinatesElement = jobject.get("coordinates");
				if (!coordinatesElement.toString().equals("null")) {
					JsonObject coordinates = coordinatesElement.getAsJsonObject();
					JsonArray innerCoordinates = coordinates.get("coordinates").getAsJsonArray();

						processedTweet.tweet_loc = "";
						processedTweet.tweet_loc = innerCoordinates.get(1).getAsString();
						processedTweet.tweet_loc += ",";
						processedTweet.tweet_loc += innerCoordinates.get(0).getAsString();
						System.out.println(processedTweet.tweet_loc);
				}
				String trimmedText = RemoveWordGroup(processedTweet.hashtags, processedTweet.tweet_urls,
						processedTweet.mentions, processedTweet.tweet_text);

				EmojiUtils emojiUtils = new EmojiUtils();
				processedTweet.tweet_emoticons = (List<String>) GetEmojis(trimmedText).get("list");
				trimmedText = (String) GetEmojis(trimmedText).get("text");
				trimmedText = RemoveWordGroup(processedTweet.hashtags, processedTweet.tweet_urls, processedTweet.mentions,
						trimmedText);
				switch (processedTweet.tweet_lang) {
				case "en":
					processedTweet.text_en = trimmedText;
					break;
				case "ko":
					processedTweet.text_ko = trimmedText;
					break;
				case "tr":
					processedTweet.text_tr = trimmedText;
					break;
				case "es":
					processedTweet.text_es = trimmedText;
					break;
				}
				processedTweets.add(processedTweet);
				++numberOfTweets;
			}
			try (Writer writer = new FileWriter(fileName + "_correct"+numberOfTweets+".json")) {
				GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.disableHtmlEscaping();
				Gson gson2 = gsonBuilder.create();
				gson2.toJson(processedTweets, writer);
			}
			System.out.println(fileName + "_p"+numberOfTweets+".json");
		} 
		public static String RemoveWordGroup(List<String> hashtags, List<String> urls, List<String> mentions,

				String sampleText) {

				for (String hashtag : hashtags) {

				sampleText = sampleText.replaceAll("#"+hashtag, "");

				}

				for (String mention : mentions) {

				sampleText = sampleText.replaceAll("@"+mention, "");

				}

				for (String url : urls) {

				sampleText = sampleText.replaceAll(url, "");

				}

				sampleText = sampleText.replaceAll("\\n", "");

				//sampleText = sampleText.replaceAll("[][(){},.;!?<>%]", "");

				//("[]'<>~:|=$+-[(){},.;!?<>%]", "");

			


				return sampleText;

				}


		
	public static Hashtable<String, Object> GetEmojis(String text) {
		Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
		List<String> ListEmojis = new ArrayList<String>();
		String EmoticonlessText = text;
		

		//	Pattern emoticonPattern ="([\\Q,:D\\E|\\Q':D\\E|\\Q]:D\\E|\\Qo:D\\E|\\QO:D\\E|\\Q0:D\\E|\\Q:D\\E|\\Q,:-D\\E|\\Q':-D\\E|\\Q]:-D\\E|\\Qo:-D\\E|\\QO:-D\\E|\\Q0:-D\\E|\\Q:-D\\E|\\Q,=D\\E|\\Q'=D\\E|\\Q]=D\\E|\\Qo=D\\E|\\QO=D\\E|\\Q0=D\\E|\\Q=D\\E|\\Q,=-D\\E|\\Q'=-D\\E|\\Q]=-D\\E|\\Qo=-D\\E|\\QO=-D\\E|\\Q0=-D\\E|\\Q=-D\\E|\\Q,:)\\E|\\Q':)\\E|\\Q]:)\\E|\\Qo:)\\E|\\QO:)\\E|\\Q0:)\\E|\\Q:)\\E|\\Q,:]\\E|\\Q':]\\E|\\Q]:]\\E|\\Qo:]\\E|\\QO:]\\E|\\Q0:]\\E|\\Q:]\\E|\\Q,:-)\\E|\\Q':-)\\E|\\Q]:-)\\E|\\Qo:-)\\E|\\QO:-)\\E|\\Q0:-)\\E|\\Q:-)\\E|\\Q,:-]\\E|\\Q':-]\\E|\\Q]:-]\\E|\\Qo:-]\\E|\\QO:-]\\E|\\Q0:-]\\E|\\Q:-]\\E|\\Q,=)\\E|\\Q'=)\\E|\\Q]=)\\E|\\Qo=)\\E|\\QO=)\\E|\\Q0=)\\E|\\Q=)\\E|\\Q,=]\\E|\\Q'=]\\E|\\Q]=]\\E|\\Qo=]\\E|\\QO=]\\E|\\Q0=]\\E|\\Q=]\\E|\\Q,=-)\\E|\\Q'=-)\\E|\\Q]=-)\\E|\\Qo=-)\\E|\\QO=-)\\E|\\Q0=-)\\E|\\Q=-)\\E|\\Q,=-]\\E|\\Q'=-]\\E|\\Q]=-]\\E|\\Qo=-]\\E|\\QO=-]\\E|\\Q0=-]\\E|\\Q=-]\\E|\\Q:")\\E|\\Q:"]\\E|\\Q:"D\\E|\\Q:-")\\E|\\Q:-"]\\E|\\Q:-"D\\E|\\Q=")\\E|\\Q="]\\E|\\Q="D\\E|\\Q=-")\\E|\\Q=-"]\\E|\\Q=-"D\\E|\\Q;)\\E|\\Q;]\\E|\\Q;D\\E|\\Q;-)\\E|\\Q;-]\\E|\\Q;-D\\E|\\Q:*\\E|\\Q:-*\\E|\\Q=*\\E|\\Q=-*\\E|\\Q;p\\E|\\Q;P\\E|\\Q;d\\E|\\Q;-p\\E|\\Q;-P\\E|\\Q;-d\\E|\\QxP\\E|\\Qx-p\\E|\\Qx-P\\E|\\Qx-d\\E|\\QXp\\E|\\QXd\\E|\\QX-p\\E|\\QX-P\\E|\\QX-d\\E|\\Q:p\\E|\\Q:P\\E|\\Q:d\\E|\\Q:-p\\E|\\Q:-P\\E|\\Q:-d\\E|\\Q=p\\E|\\Q=P\\E|\\Q=d\\E|\\Q=-p\\E|\\Q=-P\\E|\\Q=-d\\E|\\Q:$\\E|\\Q:s\\E|\\Q:z\\E|\\Q:S\\E|\\Q:Z\\E|\\Q:-$\\E|\\Q:-s\\E|\\Q:-z\\E|\\Q:-S\\E|\\Q:-Z\\E|\\Q=$\\E|\\Q=s\\E|\\Q=z\\E|\\Q=S\\E|\\Q=Z\\E|\\Q=-$\\E|\\Q=-s\\E|\\Q=-z\\E|\\Q=-S\\E|\\Q=-Z\\E|\\Q:,(\\E|\\Q:,[\\E|\\Q:,|\\E|\\Q:,-(\\E|\\Q:,-[\\E|\\Q:,-|\\E|\\Q:'(\\E|\\Q:'[\\E|\\Q:'|\\E|\\Q:'-(\\E|\\Q:'-[\\E|\\Q:'-|\\E|\\Q=,(\\E|\\Q=,[\\E|\\Q=,|\\E|\\Q=,-(\\E|\\Q=,-[\\E|\\Q=,-|\\E|\\Q='(\\E|\\Q='[\\E|\\Q='|\\E|\\Q='-(\\E|\\Q='-[\\E|\\Q='-|\\E|\\Q:,)\\E|\\Q:,]\\E|\\Q:,D\\E|\\Q:,-)\\E|\\Q:,-]\\E|\\Q:,-D\\E|\\Q:')\\E|\\Q:']\\E|\\Q:'D\\E|\\Q:'-)\\E|\\Q:'-]\\E|\\Q:'-D\\E|\\Q=,)\\E|\\Q=,]\\E|\\Q=,D\\E|\\Q=,-)\\E|\\Q=,-]\\E|\\Q=,-D\\E|\\Q=')\\E|\\Q=']\\E|\\Q='D\\E|\\Q='-)\\E|\\Q='-]\\E|\\Q='-D\\E|\\Q:,'(\\E|\\Q:,'[\\E|\\Q:,'-(\\E|\\Q:,'-[\\E|\\Q:',(\\E|\\Q:',[\\E|\\Q:',-(\\E|\\Q:',-[\\E|\\Q=,'(\\E|\\Q=,'[\\E|\\Q=,'-(\\E|\\Q=,'-[\\E|\\Q=',(\\E|\\Q=',[\\E|\\Q=',-(\\E|\\Q=',-[\\E|\\Q,:(\\E|\\Q,:[\\E|\\Q,:-(\\E|\\Q,:-[\\E|\\Q,=(\\E|\\Q,=[\\E|\\Q,=-(\\E|\\Q,=-[\\E|\\Q':(\\E|\\Q':[\\E|\\Q':-(\\E|\\Q':-[\\E|\\Q'=(\\E|\\Q'=[\\E|\\Q'=-(\\E|\\Q'=-[\\E|\\Q>:(\\E|\\Q>:[\\E|\\Q>:-(\\E|\\Q>:-[\\E|\\Q>=(\\E|\\Q>=[\\E|\\Q>=-(\\E|\\Q>=-[\\E|\\Q:@\\E|\\Q:-@\\E|\\Q=@\\E|\\Q=-@\\E|\\Qx)\\E|\\Qx]\\E|\\QxD\\E|\\Qx-)\\E|\\Qx-]\\E|\\Qx-D\\E|\\QX)\\E|\\QX]\\E|\\QX-)\\E|\\QX-]\\E|\\QX-D\\E|\\Q8)\\E|\\Q8]\\E|\\Q8D\\E|\\Q8-)\\E|\\Q8-]\\E|\\Q8-D\\E|\\QB)\\E|\\QB]\\E|\\QB-)\\E|\\QB-]\\E|\\QB-D\\E|\\Q]:(\\E|\\Q:(\\E|\\Q]:[\\E|\\Q:[\\E|\\Q]:-(\\E|\\Q:-(\\E|\\Q]:-[\\E|\\Q:-[\\E|\\Q]=(\\E|\\Q=(\\E|\\Q]=[\\E|\\Q=[\\E|\\Q]=-(\\E|\\Q=-(\\E|\\Q]=-[\\E|\\Q=-[\\E|\\Q:o\\E|\\Q:O\\E|\\Q:0\\E|\\Q:-o\\E|\\Q:-O\\E|\\Q:-0\\E|\\Q=o\\E|\\Q=O\\E|\\Q=0\\E|\\Q=-o\\E|\\Q=-O\\E|\\Q=-0\\E|\\Q:|\\E|\\Q:-|\\E|\\Q=|\\E|\\Q=-|\\E|\\Q:/\\E|\\Q:\\\E|\\Q:-/\\E|\\Q:-\\\E|\\Q=/\\E|\\Q=\\\E|\\Q=-/\\E|\\Q=-\\\E|\\Q:-3\\E|\\Q:-\\E|\\Q:3\\E|\\Q=3\\E|\\Q=-3\\E|\\Q;3\\E|\\Q;-3\\E|\\Qx3\\E|\\Qx-3\\E|\\QX3\\E|\\QX-3\\E|\\Q<3\\E|\\Q<\3\\E|\\Q</3\\E])"
		Pattern emoticonsPattern= EmojiManager.getEmoticonRegexPattern();
		Matcher emoticonMatcher = emoticonsPattern.matcher(text);
		while (emoticonMatcher.find()) {
			String emoticonCode = emoticonMatcher.group();
			EmoticonlessText = EmoticonlessText.replace(emoticonCode, "");
			ListEmojis.add(emoticonCode);
			
		}

		String htmlifiedText = EmojiUtils.htmlify(EmoticonlessText);
		String EmojiLessText = EmoticonlessText;
		String regex = "&#\\w+;";

		
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(htmlifiedText);
		while (matcher.find()) {
			String emojiCode = matcher.group();
			if (EmojiUtils.isEmoji(emojiCode)) {
				EmojiLessText = EmojiLessText.replace(EmojiUtils.emojify(emojiCode), "");
				ListEmojis.add(EmojiUtils.emojify(emojiCode));
			}
		}
		
		EmojiLessText=Pattern.compile("['<>~:|=$+-][(){},.;!?<>%]").matcher(EmojiLessText).replaceAll("");
		hashtable.put("list", ListEmojis);
		hashtable.put("text", EmojiLessText);

		return hashtable;
	}
	
//	public static String RemoveWordGroup(List<String> hashtags, List<String> urls, List<String> mentions,
//	String sampleText) {
//StringBuffer clean = new StringBuffer();
//int index = 0;
//
//while (index < sampleText.length()) {
//	int nextIndex = sampleText.indexOf(" ", index);
//	if (nextIndex == -1) {
//		nextIndex = sampleText.length();
//	}
//	String word = sampleText.substring(index, nextIndex);
//	boolean toAppend = false;
//	if (word.length() > 1)
//		toAppend = !hashtags.contains(word.substring(1)) && !urls.contains(word) && !mentions.contains(word);
//	else
//		toAppend = !urls.contains(word) && !mentions.contains(word);
//	if (toAppend) {
//		clean.append(word);
//		if (nextIndex < sampleText.length()) {
//			// this adds the word delimiter, e.g. the following space
//			clean.append(sampleText.substring(nextIndex, nextIndex + 1));
//		}
//	}
//	index = nextIndex + 1;
//}
//
//return clean.toString();
//}


}

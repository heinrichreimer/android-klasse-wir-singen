package de.klassewirsingen.app.webview;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InternalResourcesInflater {
    public static String inflate(Context context, String html, @NotNull @NonNull Uri baseUrl) {
        //Remove Cufon library
        html = html.replaceAll("Cufon\\.now\\(\\);", "");
        html = html.replaceAll("wp-content/themes/theme[1-9]{1,4}/js/cufon-replace\\.js,?", "");
        html = html.replaceAll("wp-content/themes/theme[1-9]{1,4}/js/cufon-yui\\.js,?", "");
        html = html.replaceAll(",?wp-content/themes/theme[1-9]{1,4}/js/Bebas_Neue_400\\.font\\.js", "");
        html = html.replaceAll(",?wp-content/themes/theme[1-9]{1,4}/js/Bebas_400\\.font\\.js", "");

        //Remove superfish navigation library
        html = html.replaceAll("wp-content/themes/theme[1-9]{1,4}/js/superfish\\.js,?", "");
        html = html.replaceAll("wp-content/themes/theme[1-9]{1,4}/js/supersubs\\.js,?", "");
        html = html.replaceAll(".supersubs\\(\\{((.+)\\})?\\)", "");
        html = html.replaceAll(".superfish\\(\\{((.+)\\})?\\)", "");

        //Remove audiojs
        html = html.replaceAll("audiojs\\.events\\.ready\\(function\\(\\) \\{\\s+var as = audiojs\\.createAll\\(\\);\\s+\\}\\);", "");




        Document doc = Jsoup.parse(html, baseUrl.toString());

        //Add styles
        Element googleFonts = new Element(Tag.valueOf("link"), baseUrl.toString())
                .attr("href", "https://fonts.googleapis.com/css?family=Roboto:300,400,400i,500,700")
                .attr("rel", "stylesheet");
        Element materialIcons = new Element(Tag.valueOf("link"), baseUrl.toString())
                .attr("href", "https://cdn.materialdesignicons.com/1.6.50/css/materialdesignicons.min.css")
                .attr("rel", "stylesheet");
        Element style = new Element(Tag.valueOf("style"), baseUrl.toString())
                .html(readAsset(context.getAssets(), "styles.min.css"))
                .attr("type", "text/css");
        doc.head()
                .appendChild(googleFonts)
                .appendChild(materialIcons)
                .appendChild(style);

        //Add scripts
        Element scripts = new Element(Tag.valueOf("script"), baseUrl.toString())
                .html(readAsset(context.getAssets(), "scripts.min.js"))
                .attr("type", "text/javascript");
        doc.body()
                .appendChild(scripts);

        //Replace search buttons
        doc.select("body #main #header #widget-header input.submit")
                .attr("value", "\uF349")
                .attr("placeholder", "Suche");
        doc.select("body #main #content .searchform input.submit")
                .attr("value", "\uF349")
                .attr("placeholder", "Suche");



        Logger.d("New HTML code:\n%s", doc.outerHtml());

        return doc.outerHtml();
    }

    @NonNull
    private static String readAsset(AssetManager assets, String filename) {
        try {
            StringBuilder builder = new StringBuilder();
            InputStream inputStream = assets.open(filename);
            BufferedReader in= new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str;

            while ((str=in.readLine()) != null) {
                builder.append(str);
            }
            in.close();

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}

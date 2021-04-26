package na.endpoints;

import na.service.Assertions;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Endpoint(id = "newsParams")
public class NewsParamsEndpoint {
    private final static Logger logger =
            Logger.getLogger(NewsParamsEndpoint.class);

    private final Map<String, String> paramsInfo;
    private final List<String> countries;
    private final List<String> languages;
    private final List<String> categories;
    private final List<String> formats;

    public NewsParamsEndpoint() {
        paramsInfo = new ConcurrentHashMap<>(4);
        countries = new LinkedList<>();
        languages = new LinkedList<>();
        categories = new LinkedList<>();
        formats = new ArrayList<>();

        paramsInfo.put("country", "default: all");
        paramsInfo.put("category", "default: all");
        paramsInfo.put("language", "default: all");
        paramsInfo.put("format", "default: json");
    }

    @Autowired
    public void setCountries(@Value("ae, ar, at, au, be, bg, br, ca," +
            "ch, cn, co, cu, cz, de, eg, fr, gb, gr, hk, hu, id, ie," +
            "il, in, it, jp, kr, lt, lv, ma, mx, my, ng, nl, no, nz," +
            "ph, pl, pt, ro, rs, ru, sa, se, sg, si, sk, th, tr, tw," +
            "ua, us, ve, za, all") String ... countries) {
        Assertions.isNotNull(countries, "Countries list", logger);

        this.countries.addAll(Arrays.asList(countries));
    }

    @Autowired
    public void setLanguages(@Value("ar, de, en, es, fr, he, it, nl," +
            " no, pt, ru, se, ud, zh, all") String ... languages) {
        Assertions.isNotNull(languages, "Languages list", logger);

        this.languages.addAll(Arrays.asList(languages));
    }

    @Autowired
    public void setCategories(@Value("business, entertainment," +
            "general, health, science, sports, technology, all")
                                          String ... categories) {
        Assertions.isNotNull(categories, "Categories list", logger);

        this.categories.addAll(Arrays.asList(categories));
    }

    @Autowired
    public void setFormats(@Value("json, xml, docx")
                                       String ... formats) {
        Assertions.isNotNull(formats, "Formats list", logger);

        this.formats.addAll(Arrays.asList(formats));
    }

    @ReadOperation
    public Map<String, String> allParams() {
        return paramsInfo;
    }

    @ReadOperation
    public List<String> params(@Selector String param) {
        switch(param) {
            case "country":
                return countries;
            case "category":
                return categories;
            case "language":
                return languages;
            case "format":
                return formats;
            default:
                return new ArrayList<>(0);
        }
    }
}

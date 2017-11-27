package br.com.jonyfs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@ToString
@Value
@Builder
public class MyMessage implements Serializable {

    private static final long serialVersionUID = -8013965441896177936L;

    String id;
    String content;
    Date date;

    @JsonCreator
    public MyMessage(@JsonProperty("id") String id, @JsonProperty("content") String content, @JsonProperty("date") Date date) {
        this.id = id;
        this.content = content;
        this.date = date;
    }

}

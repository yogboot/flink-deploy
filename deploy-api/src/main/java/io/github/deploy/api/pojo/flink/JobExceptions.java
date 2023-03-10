package io.github.deploy.api.pojo.flink;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class JobExceptions {

    @JsonAlias("root-exception")
    private String rootException;
}

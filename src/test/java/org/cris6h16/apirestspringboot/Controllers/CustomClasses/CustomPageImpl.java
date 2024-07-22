package org.cris6h16.apirestspringboot.Controllers.CustomClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
/*

|| ------------------------------ 1 ------------------------------ ||
 ParameterizedTypeReference<Page<PublicUserDTO>> type = new ParameterizedTypeReference<>() {};
        URI uri = UriComponentsBuilder.fromPath(USER_PATH)
                .queryParam("page", 0)
                .queryParam("size", 25)
                .queryParam("sort", "email,desc")
                .build().toUri();

        ResponseEntity<Page<PublicUserDTO>> list = this.restTemplate
                .withBasicAuth("cris6h16", "12345678")
                .exchange(uri, HttpMethod.GET, null, type);
...............................................................................
    org.springframework.http.converter.HttpMessageConversionException: Type definition error: [simple type, class org.springframework.data.domain.Page]
|| --------------------------------------------------------------- ||



|| ------------------------------ 2 ------------------------------ ||
  Page<PublicUserDTO> pageRes = objectMapper.readValue(
                pageStr,
                objectMapper.getTypeFactory()
                        .constructParametricType( // Page<PublicUserDTO>
                                Page.class,
                                PublicUserDTO.class
                        )
        );
...............................................................................
com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `org.springframework.data.domain.Page` (no Creators, like default constructor, exist): abstract types either need to be mapped to concrete types, have custom deserializer, or contain additional type information
 at [Source: (String)"{"content":[{"id":0,"username":"cris6h160","email":"cris6h160@gmail.com","createdAt":"2024-07-22T14:18:46.487+00:00","updatedAt":"2024-07-22T14:18:46.487+00:00","roles":[{"name":"ROLE_USER"}],"notes":null},{"id":1,"username":"cris6h161","email":"cris6h161@gmail.com","createdAt":"2024-07-22T14:18:46.487+00:00","updatedAt":"2024-07-22T14:18:46.487+00:00","roles":[{"name":"ROLE_USER"}],"notes":null},{"id":2,"username":"cris6h162","email":"cris6h162@gmail.com","createdAt":"2024-07-22T14:18:46.487+00"[truncated 791 chars]; line: 1, column: 1]
|| --------------------------------------------------------------- ||

 */
public class CustomPageImpl<T> extends PageImpl<T> {

    private static final long serialVersionUID = 3248189030448292002L;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CustomPageImpl(@JsonProperty("content") List<T> content, @JsonProperty("number") int number, @JsonProperty("size") int size,
                          @JsonProperty("totalElements") Long totalElements, @JsonProperty("pageable") JsonNode pageable, @JsonProperty("last") boolean last,
                          @JsonProperty("totalPages") int totalPages, @JsonProperty("sort") JsonNode sort, @JsonProperty("first") boolean first,
                          @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size), totalElements);
    }

    public CustomPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public CustomPageImpl(List<T> content) {
        super(content);
    }

    public CustomPageImpl() {
        super(new ArrayList<T>());
    }

}

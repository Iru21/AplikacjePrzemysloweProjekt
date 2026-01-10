package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.SearchPreferenceDto;
import me.iru.datingapp.entity.SearchPreference;
import me.iru.datingapp.entity.User;
import org.springframework.stereotype.Component;

@Component
public class SearchPreferenceMapper {

    public SearchPreferenceDto toDto(SearchPreference preference) {
        if (preference == null) {
            return null;
        }

        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setId(preference.getId());
        dto.setPreferredGender(preference.getPreferredGender());
        dto.setMinAge(preference.getMinAge());
        dto.setMaxAge(preference.getMaxAge());
        dto.setMaxDistance(preference.getMaxDistance());

        return dto;
    }


    public SearchPreference toEntity(SearchPreferenceDto dto, User user) {
        if (dto == null) {
            return null;
        }

        SearchPreference preference = new SearchPreference();
        preference.setUser(user);
        preference.setPreferredGender(dto.getPreferredGender());
        preference.setMinAge(dto.getMinAge());
        preference.setMaxAge(dto.getMaxAge());
        preference.setMaxDistance(dto.getMaxDistance());

        return preference;
    }


    public void updateEntityFromDto(SearchPreferenceDto dto, SearchPreference preference) {
        if (dto == null || preference == null) {
            return;
        }

        if (dto.getPreferredGender() != null) {
            preference.setPreferredGender(dto.getPreferredGender());
        }

        if (dto.getMinAge() != null) {
            preference.setMinAge(dto.getMinAge());
        }

        if (dto.getMaxAge() != null) {
            preference.setMaxAge(dto.getMaxAge());
        }

        if (dto.getMaxDistance() != null) {
            preference.setMaxDistance(dto.getMaxDistance());
        }
    }
}


package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDetailDto {
    private Long id;
    private String groupName;
}

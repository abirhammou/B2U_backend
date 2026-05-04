package tn.esprit.spring.b2u.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyResponse {

    private String memberId;
    private String memberName;
    private String didYesterday;
    private String doingToday;
    private String blockers;
}

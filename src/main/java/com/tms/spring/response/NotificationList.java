package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class NotificationList {
	private Long id;
    private String name;
    private LocalDateTime alertDate;
    private LocalDateTime deadline;
    private Boolean isViewed;

	public NotificationList(Long id, String name, LocalDateTime alertDate, LocalDateTime deadline, Boolean isViewed) {
		this.id = id;
		this.name = name;
		this.alertDate = alertDate;
		this.deadline = deadline;
		this.isViewed = isViewed;
	}
}
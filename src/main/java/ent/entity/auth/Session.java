package ent.entity.auth;

import ent.enums.Role;
import ent.enums.State;
import ent.service.Service;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Session {

    private final Service service;
    public Map<Long, Optional<SessionUser>> sessions = new HashMap<>();

    public Session(Service service) {
        this.service = service;
    }

    public Optional<SessionUser> findByChatId(Long chatId) {
        if (Objects.nonNull(sessions.get(chatId)))
            return sessions.get(chatId);
        return Optional.empty();
    }

    public Boolean existsByChatId(Long chatId) {
        if (Objects.nonNull(sessions.get(chatId)))
            return sessions.get(chatId).isPresent();
        return false;
    }

    public SessionUser getByChatId(Long chatId) {
        if (Objects.nonNull(sessions.get(chatId))) {
            Optional<SessionUser> sessionUser = sessions.get(chatId);
            return sessionUser.orElse(null);
        }
        return null;
    }

    public Boolean checkState(State state, Long chatId) {
        Optional<SessionUser> session = findByChatId(chatId);
        return session.map(sessionUser -> sessionUser.getState().equals(state.getCode())).orElse(false);
    }

    public void setSession(Long chatId) {
        if (service.existsByChatId(chatId))
            sessions.put(chatId, prepare(service.getByChatId(chatId)));
    }

    public void setSession(Long chatId, Optional<SessionUser> session) {
        sessions.put(chatId, session);
    }

    public Optional<SessionUser> prepare(AuthUser user) {
        return Optional.of(SessionUser.builder()
                .state(State.DEFAULT.getCode())
                .chatId(user.getChatId())
                .page(user.getPage())
                .role(user.getRole()).build());
    }

    public void setState(State state, Long chatId) {
        Optional<SessionUser> session = findByChatId(chatId);
        if (session.isPresent()) {
            session.get().setState(state.getCode());
            setSession(chatId, session);
        }
    }

    public Boolean checkRole(Long chatId, Role... role) {
        Role[] clone = role.clone();
        Optional<SessionUser> user = sessions.get(chatId);
        return Arrays.stream(clone).anyMatch(r -> user.map(u -> u.getRole().equals(r)).orElse(false));
    }

    public void setTempVal(Long tempVal, Long chatId) {
        Optional<SessionUser> session = findByChatId(chatId);
        if (session.isPresent()) {
            session.get().setTempLong(tempVal);
            setSession(chatId, session);
        }
    }

    public void setTempVal(String tempVal, Long chatId) {
        Optional<SessionUser> session = findByChatId(chatId);
        if (session.isPresent()) {
            session.get().setTempString(tempVal);
            setSession(chatId, session);
        }
    }

    public void removeSession(Long chatId) {
        sessions.remove(chatId);
    }

    public void previousPage(Long chatId) {
        Optional<SessionUser> sessionUser = sessions.get(chatId);
        if (sessionUser.isPresent()) {
            SessionUser user = sessionUser.get();
            user.setPage(user.getPage() - 1);
            setSession(chatId, Optional.of(user));
        }
    }

    public void nextPage(Long chatId) {
        Optional<SessionUser> sessionUser = sessions.get(chatId);
        if (sessionUser.isPresent()) {
            SessionUser user = sessionUser.get();
            user.setPage(user.getPage() + 1);
            setSession(chatId, Optional.of(user));
        }
    }

    public void setPageZero(Long chatId) {
        Optional<SessionUser> user = sessions.get(chatId);
        if (user.isPresent()) {
            user.get().setPage(0);
            setSession(chatId, user);
        }
    }

}

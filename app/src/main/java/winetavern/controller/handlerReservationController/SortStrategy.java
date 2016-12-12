package winetavern.controller.handlerReservationController;

import winetavern.model.reservation.Reservation;

import java.util.List;

/**
 * @author Niklas Wünsche
 */

public interface Handler {
    public List<Reservation> handle();
}
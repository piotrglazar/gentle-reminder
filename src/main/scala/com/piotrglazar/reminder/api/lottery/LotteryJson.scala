package com.piotrglazar.reminder.api.lottery

object LotteryJson {

  case class PrimarySelectionRules(numberOfSelections: Int)

  case class BetType(id: String, primarySelectionRules: PrimarySelectionRules, combinations: Int)

  case class SubscriptionRules(durations: List[Int], durationUnit: String, betTypes: List[BetType],
                               selectionDescriptions: List[String])

  case class GameRuleSet(basePrice: Int, maxPrice: Int, minBoards: Int, maxBoards: Int, minPrimarySelections: Int,
                         maxPrimarySelections: Int, quickPickAvailable: Boolean, multiplierAvailable: Boolean,
                         multiplierPrice: Int, durations: List[Int], primarySelectionsLowNumber: Int,
                         primarySelectionsHighNumber: Int, gameId: String, subscriptionRules: SubscriptionRules,
                         basePointsPrice: Int)

  case class Draw(gameName: String, id: String, status: String, openTime: Long, closeTime: Long, drawTime: Long,
    wagerAvailable: Boolean, wagerCloseTime: Long, estimatedJackpot: Long, subscriptionAvailable: Boolean)


}

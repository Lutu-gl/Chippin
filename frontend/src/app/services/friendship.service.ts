import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Message} from '../dtos/message';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import { FriendRequest } from '../dtos/friend-request';

@Injectable({
  providedIn: 'root'
})
export class FriendshipService {

  private friendshipBaseUri: string = this.globals.backendUri + '/friendship';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }


  sendFriendRequest(friendRequest: FriendRequest): Observable<void> {
    return this.httpClient.post<void>(this.friendshipBaseUri, friendRequest);
  }

//   /**
//    * Loads all messages from the backend
//    */
//   getMessage(): Observable<Message[]> {
//     return this.httpClient.get<Message[]>(this.messageBaseUri);
//   }

//   /**
//    * Loads specific message from the backend
//    *
//    * @param id of message to load
//    */
//   getMessageById(id: number): Observable<Message> {
//     console.log('Load message details for ' + id);
//     return this.httpClient.get<Message>(this.messageBaseUri + '/' + id);
//   }

//   /**
//    * Persists message to the backend
//    *
//    * @param message to persist
//    */
//   createMessage(message: Message): Observable<Message> {
//     console.log('Create message with title ' + message.title);
//     return this.httpClient.post<Message>(this.messageBaseUri, message);
//   }
}

import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
  discardResponseBodies: true,
};

export default function() {
  var url = 'http://localhost:8081';
  var payload = JSON.stringify({
    a: 'aaa',
  });

  var params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  http.post(url, payload, params);

  //sleep(1);
}
